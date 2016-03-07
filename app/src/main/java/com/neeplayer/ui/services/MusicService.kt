package com.neeplayer.ui.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
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
import com.neeplayer.R
import com.neeplayer.model.Model
import com.neeplayer.model.Playlist
import com.neeplayer.ui.fragments.NowPlayingFragment
import org.jetbrains.anko.toast

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private val NOTIFY_ID = 1

    private val PLAY_PREVIOUS_ACTION = "PLAY_PREVIOUS"
    private val PLAY_OR_PAUSE_ACTION = "PLAY_OR_PAUSE"
    private val PLAY_NEXT_ACTION = "PLAY_NEXT"

    private val player: MediaPlayer = MediaPlayer()

    private var paused = true

    private var playlist: Playlist? = null

    private val musicBind = MusicBinder()

    override fun onCreate() {
        super.onCreate()
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

    fun trySetPlaylist(playlist: Playlist): Boolean {
        if (this.playlist != playlist) {
            this.playlist = playlist
            return true
        } else {
            return false
        }
    }

    fun playSong() {
        paused = false
        chooseSong()
    }

    fun chooseSong() {
        Model.nowPlaying = playlist
        player.reset()

        val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, playlist!!.currentSong.id)

        try {
            player.setDataSource(applicationContext, songUri)
            player.prepareAsync()
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
            player.reset()
            chooseNext()
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        applicationContext.toast("Media player error")
        return true
    }

    override fun onDestroy() {
        player.stop()
        player.release()
        stopForeground(true)
        Model.save()
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (!paused) {
            player.start()
        }
        update()
    }

    private fun update() {
        val playlist = playlist!!

        val intent = Intent(NowPlayingFragment.UPDATE_NOW_PLAYING)
        intent.putExtra("POSITION", playlist.currentPosition)
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
                .setTicker(playlist.currentSong.title)
                .setSmallIcon(R.drawable.ic_play_arrow_white)
                .setContent(smallContent)
                .build()

        notification.bigContentView = bigContent

        startForeground(NOTIFY_ID, notification)
    }

    private fun fillNotificationContent(content: RemoteViews) {
        val playlist = playlist!!

        val albumArt = BitmapFactory.decodeFile(playlist.currentAlbum.art);
        content.setImageViewBitmap(R.id.notification_album_art, albumArt);
        content.setTextViewText(R.id.notification_song_title, playlist.currentSong.title)
        content.setTextViewText(R.id.notification_artist, playlist.artist.name)
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
        playlist = playlist?.previous()

        if (paused != null) {
            this.paused = paused
        }
        chooseSong()
    }

    fun chooseNext(paused: Boolean? = null) {
        playlist = playlist?.next()

        if (paused != null) {
            this.paused = paused
        }
        chooseSong()
    }
}
