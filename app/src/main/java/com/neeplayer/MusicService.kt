package com.neeplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.support.v4.content.LocalBroadcastManager
import org.jetbrains.anko.toast

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private val NOTIFY_ID = 1

    private val player: MediaPlayer = MediaPlayer()

    private var albums: List<Album>? = null

    private var artistName: String? = null
    private var songPosition: Int = 0
    private var albumPosition: Int = 0
    private var songTitle: String? = null

    override fun onCreate() {
        super.onCreate()
        songPosition = 0
        initMediaPLayer()
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
        player.reset()
        val albums = this.albums ?: return

        val song = albums.get(albumPosition).songs.get(songPosition)
        val id = song.id

        songTitle = song.title

        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

        try {
            player.setDataSource(applicationContext, trackUri)
            player.prepareAsync()
        } catch (e: Exception) {
            applicationContext.toast("Error on setting data source")
        }

    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    private val musicBind = MusicBinder()

    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (player.currentPosition > 0) {
            mp.reset()
            playNext()
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()

        val intent = Intent("UPDATE_CURRENT_SONG")
        intent.putExtra("SONG_POSITION", songPosition)
        intent.putExtra("ALBUM_POSITION", albumPosition)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        val notIntent = Intent(this, ArtistActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(this)

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(songTitle)
                .setContentText(artistName)

        val not = builder.build()

        startForeground(NOTIFY_ID, not)


    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    fun getSongPosition(): Int {
        return player.currentPosition
    }

    val duration: Int
        get() = player.duration

    val isPlaying: Boolean?
        get() = player.isPlaying

    fun pausePlayer() {
        player.pause()
    }

    fun seekTo(position: Int) {
        player.seekTo(position)
    }

    fun start() {
        player.start()
    }

    fun playPrevious() {
        val albums = this.albums ?: return

        --songPosition
        if (songPosition < 0) {
            --albumPosition
            if (albumPosition < 0) {
                albumPosition = albums.size - 1
            }
            songPosition = albums.get(albumPosition).songs.size - 1
        }
        playSong()
    }

    fun playNext() {
        val albums = this.albums ?: return

        ++songPosition
        if (songPosition >= albums.get(albumPosition).songs.size) {
            songPosition = 0
            ++albumPosition
            if (albumPosition >= albums.size) {
                albumPosition = 0
            }
        }
        playSong()
    }
}
