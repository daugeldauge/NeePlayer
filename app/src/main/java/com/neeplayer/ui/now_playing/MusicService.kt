package com.neeplayer.ui.now_playing

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import com.neeplayer.*
import com.neeplayer.model.Song
import com.neeplayer.ui.MainActivity
import org.jetbrains.anko.toast
import javax.inject.Inject

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, NowPlayingView {

    private enum class State {
        IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED
    }

    private val NOTIFICATION_ID = 42

    private val PLAY_PREVIOUS_ACTION = "PLAY_PREVIOUS"
    private val PLAY_OR_PAUSE_ACTION = "PLAY_OR_PAUSE"
    private val PLAY_NEXT_ACTION = "PLAY_NEXT"

    private val handler = Handler()
    private val TICK_PERIOD = 100L

    private val player: MediaPlayer = MediaPlayer()

    private var paused = true
    private var song: Song? = null
    private var state = State.IDLE
    private var lastKnownAudioFocusState: Int? = null
    private var foreground = false

    @Inject
    lateinit var presenter: NowPlayingPresenter

    private val mediaSession by lazy { MediaSessionCompat(this, BuildConfig.APPLICATION_ID, ComponentName(this, MediaButtonEventsReceiver::class.java), null) }

    override fun onCreate() {
        super.onCreate()
        NeePlayerApp.component.inject(this)
        presenter.bind(this)
        initMediaPLayer()
        mediaSession.setCallback(mediaSessionCallback)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.isActive = true

        registerReceiver(headsetPlugReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
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
            updateInfo(song!!)
        }
    }

    override fun pause() {
        paused = true
        if (state == State.STARTED) {
            player.pause()
            state = State.PAUSED
            stopTicking()
            updateInfo(song!!)
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
        updateInfo(song!!)
    }

    override fun onCompletion(mp: MediaPlayer) {
        mp.reset()
        state = State.IDLE
        stopTicking()
        presenter.onNextClicked()
    }
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        state = State.IDLE
        stopTicking()
        applicationContext.toast(R.string.media_player_error)
        return true
    }

    //endregion

    override fun onDestroy() {
        stopTicking()
        player.stop()
        player.release()
        mediaSession.release()
        unregisterReceiver(headsetPlugReceiver)
        foreground = false
        stopForeground(true)
        presenter.unbind()
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

    private fun updateInfo(song: Song) {
        mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.album.artist.name)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeFile(song.album.art))
                        .build()
        )

        mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                        .setState(if (paused) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                        .build()
        )

        updateNotification(song)
    }

    //region Notifications
    private fun updateNotification(song: Song) {
        val bigContent = RemoteViews(packageName, R.layout.big_notification);
        val smallContent = RemoteViews(packageName, R.layout.small_notification);

        fillNotificationContent(bigContent, song);
        fillNotificationContent(smallContent, song);

        val intent = Intent(this, MainActivity::class.java)
                .setAction(MainActivity.OPEN_NOW_PLAYING_ACTION)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(song.title)
                .setContentIntent(contentIntent)
                .setSmallIcon(lollipop({
                    R.drawable.ic_play_arrow_white
                }, {
                    R.mipmap.ic_launcher
                }))
                .setContent(smallContent)
                .build()

        targetApi(Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = bigContent
        }

        if (!paused) {
            foreground = true
            startForeground(NOTIFICATION_ID, notification)
        } else if (foreground) {
            stopForeground(false)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun fillNotificationContent(content: RemoteViews, song: Song) {

        val albumArt = BitmapFactory.decodeFile(song.album.art);
        content.setImageViewBitmap(R.id.notification_album_art, albumArt);
        content.setTextViewText(R.id.notification_song_title, song.title)
        content.setTextViewText(R.id.notification_artist, song.album.artist.name)
        content.setImageViewVectorResource(R.id.notification_play_pause_button, if (paused) R.drawable.ic_play_arrow_black_medium else R.drawable.ic_pause_black_medium)
        content.setImageViewVectorResource(R.id.notification_fast_forward_button, R.drawable.ic_fast_forward_black_medium)
        content.setImageViewVectorResource(R.id.notification_fast_rewind_button, R.drawable.ic_fast_rewind_black_medium)

        setupPendingIntent(content, R.id.notification_fast_rewind_button, PLAY_PREVIOUS_ACTION)
        setupPendingIntent(content, R.id.notification_play_pause_button, PLAY_OR_PAUSE_ACTION)
        setupPendingIntent(content, R.id.notification_fast_forward_button, PLAY_NEXT_ACTION)
    }

    private fun RemoteViews.setImageViewVectorResource(@IdRes viewId: Int, @DrawableRes vectorId: Int) {
        lollipop({ setImageViewResource(viewId, vectorId) }) {

            val drawable = VectorDrawableCompat.create(resources, vectorId, theme) ?: return
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            setImageViewBitmap(viewId, bitmap)

        }
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
                tick()
                state = State.STARTED
            }
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                toast(R.string.audio_focus_request_error)
                presenter.onPlayPauseClicked()
            }
        }
    }

    private fun tick() {
        presenter.onSeek(player.currentPosition)
        handler.postDelayed({ tick() }, TICK_PERIOD)
    }

    private fun stopTicking() {
        handler.removeCallbacksAndMessages(null)
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

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        //TODO implement methods
    }

    private val headsetPlugReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!paused) {
                presenter.onPlayPauseClicked()
            }
        }
    }

    class MediaButtonEventsReceiver : BroadcastReceiver () {
        override fun onReceive(context: Context, intent: Intent) {
            //TODO handle events
        }

    }
}
