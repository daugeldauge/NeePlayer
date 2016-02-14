package com.neeplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.support.annotation.IdRes
import android.support.v4.content.LocalBroadcastManager
import android.widget.RemoteViews
import org.jetbrains.anko.toast

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private val NOTIFY_ID = 1

    private val PLAY_PREVIOUS_ACTION = "PLAY_PREVIOUS"
    private val PLAY_OR_PAUSE_ACTION = "PLAY_OR_PAUSE"
    private val PLAY_NEXT_ACTION = "PLAY_NEXT"

    private val player: MediaPlayer = MediaPlayer()

    private var albums: List<Album>? = null

    private var artistName: String? = null
    private var songPosition: Int = 0
    private var albumPosition: Int = 0
    private var nowPlaying: Song? = null
    private var paused = true

    private var albumArt: Bitmap? = null
    private var notificationContent: RemoteViews? = null

    private val musicBind = MusicBinder()

    override fun onCreate() {
        super.onCreate()
        songPosition = 0
        initMediaPLayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            PLAY_PREVIOUS_ACTION -> choosePrevious()
            PLAY_OR_PAUSE_ACTION -> playOrPause()
            PLAY_NEXT_ACTION -> chooseNext()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun playOrPause() {
        this.paused = !this.paused
        if (paused) {
            player.pause()
        } else {
            player.start()
        }
        update()
    }

    fun initMediaPLayer() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)

        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    fun setList(albums: List<Album>?) {
        this.albums = albums
    }

    fun setArtistName(artistName: String?) {
        this.artistName = artistName
    }

    fun setPosition(albumPosition: Int, songPosition: Int) {
        this.songPosition = songPosition
        this.albumPosition = albumPosition
    }

    fun playSong() {
        paused = false
        chooseSong()
    }

    fun chooseSong() {
        player.reset()
        val albums = this.albums ?: return

        val song = albums.get(albumPosition).songs.get(songPosition)
        val id = song.id

        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

        try {
            player.setDataSource(applicationContext, trackUri)
            player.prepareAsync()
            this.nowPlaying = song
        } catch (e: Exception) {
            applicationContext.toast("Error on setting data source")
        }

    }
    inner class MusicBinder : Binder() {

        internal val service: MusicService
            get() = this@MusicService

    }

    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (player.currentPosition > 0) {
            mp.reset()
            chooseNext()
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    override fun onDestroy() {
        player.stop()
        player.release()
        stopForeground(true)
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (!paused) {
            player.start()
        }
        update()
    }

    private fun update() {
        val intent = Intent(NowPlayingFragment.UPDATE_NOW_PLAYING)
        intent.putExtra("SONG_POSITION", songPosition)
        intent.putExtra("ALBUM_POSITION", albumPosition)
        intent.putExtra("PAUSED", paused)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)


        val bigContent = RemoteViews(packageName, R.layout.big_notification);
        val smallContent = RemoteViews(packageName, R.layout.small_notification);

        fillNotificationContent(bigContent);
        fillNotificationContent(smallContent);

        val notification = Notification.Builder(this)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setTicker(nowPlaying?.title)
                .setSmallIcon(R.drawable.ic_play_arrow_white)
                .setContent(smallContent)
                .build()

        notification.bigContentView = bigContent

        startForeground(NOTIFY_ID, notification)
    }

    private fun fillNotificationContent(content: RemoteViews) {
        val albumArt = BitmapFactory.decodeFile(albums?.get(albumPosition)?.art);
        content.setImageViewBitmap(R.id.notification_album_art, albumArt);
        content.setTextViewText(R.id.notification_song_title, nowPlaying?.title)
        content.setTextViewText(R.id.notification_artist, artistName)
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    val currentPosition: Long
        get() = player.currentPosition.toLong()

    val duration: Int
        get() = player.duration

    val isPlaying: Boolean
        get() = player.isPlaying


    fun seekTo(position: Int) {
        player.seekTo(position)
    }

    fun play() {
        paused = false
        player.start()
        update()
    }

    fun pause() {
        paused = true
        player.pause()
        update()
    }

    fun choosePrevious(paused: Boolean? = null) {
        val albums = this.albums ?: return

        --songPosition
        if (songPosition < 0) {
            --albumPosition
            if (albumPosition < 0) {
                albumPosition = albums.size - 1
            }
            songPosition = albums.get(albumPosition).songs.size - 1
        }

        if (paused != null) {
            this.paused = paused
        }
        chooseSong()
    }

    fun chooseNext(paused: Boolean? = null) {
        val albums = this.albums ?: return

        ++songPosition
        if (songPosition >= albums.get(albumPosition).songs.size) {
            songPosition = 0
            ++albumPosition
            if (albumPosition >= albums.size) {
                albumPosition = 0
            }
        }
        if (paused != null) {
            this.paused = paused
        }
        chooseSong()
    }
}
