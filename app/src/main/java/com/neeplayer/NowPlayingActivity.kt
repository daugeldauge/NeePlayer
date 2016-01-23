package com.neeplayer

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.databinding.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import com.bumptech.glide.Glide
import com.neeplayer.databinding.ActivityNowPlayingBinding

import java.util.ArrayList

import kotlinx.android.synthetic.activity_now_playing.*
import org.jetbrains.anko.onClick
import kotlin.properties.Delegates

class NowPlayingActivity : Activity() {
    companion object{
        val UPDATE_CURRENT_SONG = "UPDATE_CURRENT_SONG"
    }

    class ViewModel(val albumList: ArrayList<Album>, val artistName: String,
            var albumPosition: Int , var songPosition: Int, var paused: Boolean = true) : BaseObservable() {

        val album: Album
            get() = albumList[albumPosition]

        val song: Song
            get() = album.songs[songPosition]
    }

    private var model: ViewModel? = null
    private var binding: ActivityNowPlayingBinding? = null

    private var musicService: MusicService? = null
    private var playIntent: Intent? = null

    private var musicBound: Boolean = false

    private val musicConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val model = model ?: return

            val binder = service as MusicService.MusicBinder
            val musicService = binder.service

            musicService.setList(model.albumList)
            musicService.setArtistName(model.artistName)
            musicService.setPosition(model.albumPosition, model.songPosition)
            musicService.playSong()
            model.paused = false

            this@NowPlayingActivity.musicService = musicService

            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityNowPlayingBinding>(this, R.layout.activity_now_playing)

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
            val model = model
            if (musicBound && model != null) {

                if (model.paused) {
                    musicService?.start()
                } else {
                    musicService?.pausePlayer()
                }

                model.paused = !model.paused
                model.notifyChange()
            }
        }

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        model = ViewModel(
            albumPosition = intent.getIntExtra("ALBUM_POSITION", 0),
            songPosition = intent.getIntExtra("SONG_POSITION", 0),
            albumList = intent.getSerializableExtra("ALBUM_LIST") as ArrayList<Album>,
            artistName = intent.getStringExtra("ARTIST_NAME")
        )

        binding?.model = model

        if (playIntent != null) {
            unbindService(musicConnection)
            stopService(playIntent)
        }

        playIntent = Intent(this, MusicService::class.java)
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
        startService(playIntent)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UPDATE_CURRENT_SONG) {
                model?.albumPosition = intent.getIntExtra("ALBUM_POSITION", 0)
                model?.songPosition = intent.getIntExtra("SONG_POSITION", 0)
                model?.notifyChange()
            }
        }
    }

    override fun onDestroy() {
        unbindService(musicConnection)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }


}
