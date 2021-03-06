package com.neeplayer.ui.now_playing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.neeplayer.BuildConfig
import com.neeplayer.R
import com.neeplayer.model.Song
import com.neeplayer.ui.MainActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

class MusicService : Service(), NowPlayingView {

    private val handler = Handler(Looper.getMainLooper())
    private val mainScope = MainScope()
    private val presenter by inject<NowPlayingPresenter>()
    private val mediaSession by lazy { MediaSessionCompat(this, BuildConfig.APPLICATION_ID, ComponentName(this, MediaButtonEventsReceiver::class.java), null) }

    private var wasForeground = false

    private val player by lazy {
        MusicPlayer(application).apply {
            setCompletionListener {
                presenter.onNextClicked()
            }
        }
    }

    private val audioFocusManager by lazy {
        AudioFocusManager(
            context = application,
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager,
            volumeModifier = player::setVolume,
            focusLossListener = {
                presenter.onPauseClicked()
                stopSelf()
            },
            focusTransientLossListener = presenter::onPlayPauseClicked,
            focusGainFromTransientLossListener = presenter::onPlayPauseClicked
        )
    }

    override fun onCreate() {
        super.onCreate()
        presenter.bind(mainScope, this)
        mediaSession.setCallback(mediaSessionCallback)
        mediaSession.isActive = true

        registerReceiver(headsetPlugReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PLAY_PREVIOUS_ACTION -> presenter.onPreviousClicked()
            PLAY_OR_PAUSE_ACTION -> presenter.onPlayPauseClicked()
            PLAY_NEXT_ACTION -> presenter.onNextClicked()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun render(song: Song, paused: Boolean) {
        val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
        player.setState(songUri, playWhenReady = !paused)

        if (paused) {
            audioFocusManager.abandonFocus()
            stopTicking()
        } else {
            audioFocusManager.requestFocus()
            tick()
        }

        updateInfo(song, paused)
    }


    override fun seek(progress: Int) {
        player.seek(progress)
    }
    //endregion

    //endregion

    override fun onDestroy() {
        stopTicking()
        player.release()
        mediaSession.release()
        unregisterReceiver(headsetPlugReceiver)
        wasForeground = false
        stopForeground(true)
        mainScope.cancel()
        presenter.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun updateInfo(song: Song, paused: Boolean) {
        val albumArt = song.album.art?.let(BitmapFactory::decodeFile)

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.album.artist.name)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album.title)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .build()
        )

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(if (paused) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build()
        )

        updateNotification(song, paused, albumArt)
    }

    //region Notifications
    private fun updateNotification(song: Song, paused: Boolean, albumArt: Bitmap?) {

        val intent = Intent(this, MainActivity::class.java)
            .setAction(MainActivity.OPEN_NOW_PLAYING_ACTION)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, getString(R.string.music_notification_channel_description), NotificationManager.IMPORTANCE_HIGH))
        }

        val mediaStyle = MediaStyle().setMediaSession(mediaSession.sessionToken)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setTicker(song.title)
            .setContentTitle(song.title)
            .setContentText(song.album.artist.name)
            .setContentIntent(contentIntent)
            .setLargeIcon(albumArt)
            .setSmallIcon(R.drawable.ic_play_arrow_white)
            .addMediaAction(R.drawable.ic_fast_rewind_black_medium, PLAY_PREVIOUS_ACTION)
            .addMediaAction(if (paused) R.drawable.ic_play_arrow_black_medium else R.drawable.ic_pause_black_medium, PLAY_OR_PAUSE_ACTION)
            .addMediaAction(R.drawable.ic_fast_forward_black_medium, PLAY_NEXT_ACTION)
            .setStyle(mediaStyle)
            .build()

        when {
            !paused -> {
                wasForeground = true
                startForeground(NOTIFICATION_ID, notification)
            }
            wasForeground -> {
                stopForeground(false)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun NotificationCompat.Builder.addMediaAction(@DrawableRes drawableRes: Int, action: String): NotificationCompat.Builder {
        return addAction(drawableRes, action, createPendingIntent(action))
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).setAction(action)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    //endregion

    private fun tick() {
        presenter.onSeek(player.currentPosition)
        handler.postDelayed({ tick() }, TICK_PERIOD)
    }

    private fun stopTicking() {
        handler.removeCallbacksAndMessages(null)
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        //TODO implement methods
    }

    private val headsetPlugReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            presenter.onPauseClicked()
        }
    }

    class MediaButtonEventsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //TODO handle events
        }

    }
}


private const val NOTIFICATION_ID = 42

private const val PLAY_PREVIOUS_ACTION = "PLAY_PREVIOUS"
private const val PLAY_OR_PAUSE_ACTION = "PLAY_OR_PAUSE"
private const val PLAY_NEXT_ACTION = "PLAY_NEXT"

private const val TICK_PERIOD = 100L
private const val CHANNEL_ID = "music_controls"
