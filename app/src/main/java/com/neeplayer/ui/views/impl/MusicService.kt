package com.neeplayer.ui.views.impl

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.support.annotation.IdRes
import android.widget.RemoteViews
import com.neeplayer.R
import com.neeplayer.model.Song
import com.neeplayer.ui.presenters.NowPlayingPresenter
import com.neeplayer.ui.views.NowPlayingView
import org.jetbrains.anko.toast

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, NowPlayingView {

    private enum class State {
        IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED
    }

    private val NOTIFY_ID = 1

    private val PLAY_PREVIOUS_ACTION = "PLAY_PREVIOUS"
    private val PLAY_OR_PAUSE_ACTION = "PLAY_OR_PAUSE"
    private val PLAY_NEXT_ACTION = "PLAY_NEXT"

    private val player: MediaPlayer = MediaPlayer()

    private var paused = true
    private var song: Song? = null
    private var state = State.IDLE
    private var lastKnownAudioFocusState: Int? = null

    lateinit
    private var presenter: NowPlayingPresenter

    override fun onCreate() {
        super.onCreate()
        initMediaPLayer()
        presenter = NowPlayingPresenter(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            PLAY_PREVIOUS_ACTION -> presenter.onPreviousClicked()
            PLAY_OR_PAUSE_ACTION -> presenter.onPlayPauseClicked()
            PLAY_NEXT_ACTION     -> presenter.onNextClicked()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    //region NowPlayingView methods
    override fun setSong(song: Song) {
        this.song = song
        player.reset()
        val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)

        try {
            player.setDataSource(applicationContext, songUri)
            state = State.INITIALIZED
            player.prepareAsync()
        } catch (e: Exception) {
            applicationContext.toast("Error on setting data source")
        }

    }

    override fun play() {
        paused = false
        if (state == State.PREPARED || state == State.PAUSED)  {
            startPlaying()
            updateNotification(song!!)
        }
    }

    override fun pause() {
        paused = true
        if (state == State.STARTED) {
            player.pause()
            state = State.PAUSED
            updateNotification(song!!)
        }
    }
    override fun seek(progress: Int) {
        if (state == State.STARTED || state == State.PAUSED) {
            player.seekTo(progress)
        }
    }
    //endregion

    //region Media player callbacks
    override fun onPrepared(mp: MediaPlayer) {
        state = State.PREPARED
        if (!paused) {
            startPlaying()
        }
        updateNotification(song!!)
    }

    override fun onCompletion(mp: MediaPlayer) {
        mp.reset()
        state = State.IDLE
        presenter.onNextClicked()
    }
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        state = State.IDLE
        applicationContext.toast(R.string.media_player_error)
        return true
    }

    //endregion

    override fun onDestroy() {
        player.stop()
        player.release()
        stopForeground(true)
        presenter.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun initMediaPLayer() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)

        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    //region Notifications
    private fun updateNotification(song: Song) {
        val bigContent = RemoteViews(packageName, R.layout.big_notification);
        val smallContent = RemoteViews(packageName, R.layout.small_notification);

        fillNotificationContent(bigContent, song);
        fillNotificationContent(smallContent, song);

        val notification = Notification.Builder(this)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setTicker(song.title)
                .setSmallIcon(R.drawable.ic_play_arrow_white)
                .setContent(smallContent)
                .build()

        notification.bigContentView = bigContent

        startForeground(NOTIFY_ID, notification)
    }

    private fun fillNotificationContent(content: RemoteViews, song: Song) {

        val albumArt = BitmapFactory.decodeFile(song.album.art);
        content.setImageViewBitmap(R.id.notification_album_art, albumArt);
        content.setTextViewText(R.id.notification_song_title, song.title)
        content.setTextViewText(R.id.notification_artist, song.album.artist.name)
        content.setImageViewResource(R.id.notification_play_pause_button, if (paused) R.drawable.ic_play_arrow_black_medium else R.drawable.ic_pause_black_medium)

        setupPendingIntent(content, R.id.notification_fast_rewind_button, PLAY_PREVIOUS_ACTION)
        setupPendingIntent(content, R.id.notification_play_pause_button, PLAY_OR_PAUSE_ACTION)
        setupPendingIntent(content, R.id.notification_fast_forward_button, PLAY_NEXT_ACTION)
    }
    private fun setupPendingIntent(content: RemoteViews, @IdRes viewId: Int, action: String) {
        val intent = Intent(this, MusicService::class.java).setAction(action)
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        content.setOnClickPendingIntent(viewId, pendingIntent)
    }

    //endregion

    private fun startPlaying() {
        when((getSystemService(AUDIO_SERVICE) as AudioManager).requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                player.start()
                state = State.STARTED
                presenter.onSeek(player.currentPosition)
            }
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                toast(R.string.audio_focus_request_error)
                presenter.onPlayPauseClicked()
            }
        }
    }

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                when (lastKnownAudioFocusState) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        presenter.onPlayPauseClicked()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        player.setVolume(1f, 1f)
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                presenter.onPlayPauseClicked()
                stopSelf()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                presenter.onPlayPauseClicked()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                player.setVolume(0.2f, 0.2f)
            }
        }
        lastKnownAudioFocusState = it
    }
}
