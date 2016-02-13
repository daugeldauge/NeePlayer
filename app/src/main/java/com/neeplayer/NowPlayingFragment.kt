package com.neeplayer

import android.app.Fragment
import android.content.*
import android.databinding.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.databinding.FragmentNowPlayingBinding
import org.jetbrains.anko.onClick
import java.util.ArrayList

class NowPlayingFragment : Fragment() {
    companion object{
        val UPDATE_NOW_PLAYING = "UPDATE_NOW_PLAYING"
    }

    class ViewModel(val albumList: ArrayList<Album>, val artistName: String,
                    var albumPosition: Int, var songPosition: Int, var paused: Boolean = false) : BaseObservable() {

        val album: Album
            get() = albumList[albumPosition]

        val song: Song
            get() = album.songs[songPosition]

        var timePlayed: Long = 0
    }

    var expanded: Boolean
        get() = binding.npContainer.expanded
        set(value) {
            binding.npContainer.expanded = value
        }

    private var model: ViewModel? = null

    lateinit
    private var binding: FragmentNowPlayingBinding

    private var musicService: MusicService? = null
    private var playIntent: Intent? = null

    private var musicBound: Boolean = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentNowPlayingBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<FragmentNowPlayingBinding>(view)
    }

    fun update(artistName: String, albumList: ArrayList<Album>, position: Index) {
        val model = ViewModel(albumList, artistName, position.albumIndex, position.songIndex ?: 0)
        binding.model = model
        this.model = model

        val filter = IntentFilter()
        filter.addAction(UPDATE_NOW_PLAYING)
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, filter)

        if (playIntent != null) {
            activity.unbindService(musicConnection)
            activity.stopService(playIntent)
        }

        playIntent = Intent(activity, MusicService::class.java)
        activity.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
        activity.startService(playIntent)


        binding.npFastRewind.onClick {
            musicService?.choosePrevious(model.paused)
        }

        binding.npFastForward.onClick {
            musicService?.chooseNext(model.paused)
        }

        binding.npPlayPause.onClick {
            if (musicBound) {

                if (model.paused) {
                    musicService?.play()
                } else {
                    musicService?.pause()
                }

                model.paused = !model.paused
                model.notifyChange()
            }
        }

        binding.npSeekBar.onUserSeek { progress ->
            val timePlayed = progress.toDouble() / binding.npSeekBar.max * model.song.duration
            model.timePlayed = Math.round(timePlayed)
            model.notifyChange()
        }

    }

    override fun onDestroy() {
        activity.unbindService(musicConnection)
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
        super.onDestroy()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UPDATE_NOW_PLAYING) {
                model?.albumPosition = intent.getIntExtra("ALBUM_POSITION", 0)
                model?.songPosition = intent.getIntExtra("SONG_POSITION", 0)
                model?.paused = intent.getBooleanExtra("PAUSED", false)
                model?.notifyChange()
            }
        }
    }

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

            this@NowPlayingFragment.musicService = musicService

            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

}
