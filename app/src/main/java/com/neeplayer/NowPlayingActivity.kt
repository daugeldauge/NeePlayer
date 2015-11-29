package com.neeplayer

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager

import com.bumptech.glide.Glide

import java.util.ArrayList

import kotlinx.android.synthetic.activity_now_playing.*
import org.jetbrains.anko.onClick

class NowPlayingActivity : Activity() {
    companion object{
        val UPDATE_CURRENT_SONG = "UPDATE_CURRENT_SONG"
    }

    private var albumList: ArrayList<Album>? = null
    private var artistName: String? = null

    private var albumPosition: Int = 0
    private var songPosition: Int = 0

    private var musicService: MusicService? = null
    private var playIntent: Intent? = null

    private var musicBound: Boolean = false
    private var paused: Boolean = false

    private val musicConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            val musicService = binder.service

            musicService.setList(albumList)
            musicService.setArtistName(artistName)
            musicService.setPosition(albumPosition, songPosition)
            musicService.playSong()

            this@NowPlayingActivity.musicService = musicService

            paused = false
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)


        val filter = IntentFilter()
        filter.addAction(UPDATE_CURRENT_SONG)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        np_fast_rewind.onClick {
            musicService?.playPrevious()
        }

        np_fast_forward.onClick {
            musicService?.playNext()
        }

        np_play_pause.onClick {
            if (musicBound) {

                val drawableId = if (paused) {
                    musicService?.start()
                    R.drawable.ic_pause_black_48dp
                } else {
                    musicService?.pausePlayer()
                    R.drawable.ic_play_arrow_black_48dp
                }

                np_play_pause.setImageDrawable(getDrawable(drawableId))

                paused = !paused
            }
        }


        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        albumPosition = intent.getIntExtra("ALBUM_POSITION", 0)
        songPosition = intent.getIntExtra("SONG_POSITION", 0)
        albumList = intent.getSerializableExtra("ALBUM_LIST") as ArrayList<Album>
        artistName = intent.getStringExtra("ARTIST_NAME")


        if (playIntent != null) {
            unbindService(musicConnection)
            stopService(playIntent)
        }

        playIntent = Intent(this, MusicService::class.java)
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
        startService(playIntent)

        updateScreen()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UPDATE_CURRENT_SONG) {
                albumPosition = intent.getIntExtra("ALBUM_POSITION", 0)
                songPosition = intent.getIntExtra("SONG_POSITION", 0)
            }
            updateScreen()
        }
    }

    private fun updateScreen() {
        val albumList = this.albumList ?: return

        val album = albumList[albumPosition]
        val song = album.songs[songPosition]

        np_song_title.text = song.title
        np_artist_and_album.text = "$artistName — ${album.title}"

        Glide.with(this).load("file://" + album.art).dontAnimate().into(np_album_art)
    }

    override fun onDestroy() {
        unbindService(musicConnection)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }


}
