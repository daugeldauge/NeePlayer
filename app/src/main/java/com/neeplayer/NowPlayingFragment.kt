package com.neeplayer

import android.app.Fragment
import android.content.*
import android.databinding.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.databinding.FragmentNowPlayingBinding
import org.jetbrains.anko.onClick
import java.io.Serializable
import java.util.ArrayList

class NowPlayingFragment : Fragment() {
    companion object{
        val UPDATE_NOW_PLAYING = "UPDATE_NOW_PLAYING"
    }

    class ViewModel(val albumList: ArrayList<Album>, val artistName: String, var albumPosition: Int,
                    var songPosition: Int, paused: Boolean = false) : BaseObservable(), Serializable {

        @Transient
        private val handler = Handler()

        private var needToStopTicking = false

        @Bindable
        var paused = paused
            set(value) {
                field = value
                if (value) {
                    needToStopTicking = true
                }
                notifyPropertyChanged(BR.paused)
            }

        val album: Album
            get() = albumList[albumPosition]

        val song: Song
            get() = album.songs[songPosition]

        @Transient
        val timePlayed = ObservableLong(0)

        fun tick() {
            if (paused) {
                return
            }

            needToStopTicking = false
            val delay = 1000 - (timePlayed.get() % 1000)
            handler.removeCallbacks(tock)
            handler.postDelayed(tock, delay)
        }

        @Transient
        private val tock = Runnable {
            if (!needToStopTicking) {
                timePlayed.set(Math.min(song.duration, 1000 * (timePlayed.get() / 1000 + 1)))
                tick()
            }
        }

    }

    private var model: ViewModel? = null

    lateinit
    private var binding: FragmentNowPlayingBinding

    lateinit
    private var bottomSheet: BottomSheetBehavior<View>

    private var musicService: MusicService? = null
    private var playIntent: Intent? = null

    private var shouldStartPlaying = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentNowPlayingBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<FragmentNowPlayingBinding>(view)
        bottomSheet = BottomSheetBehavior.from(binding.npContainer)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            setupModel(savedInstanceState.get("model") as ViewModel)

            playIntent = Intent(activity, MusicService::class.java)
            activity.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            activity.startService(playIntent)
            model?.paused = true
            shouldStartPlaying = false
        }

        val filter = IntentFilter()
        filter.addAction(UPDATE_NOW_PLAYING)
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, filter)
    }

    fun update(artistName: String, albumList: ArrayList<Album>, position: Index) {
        val model = ViewModel(albumList, artistName, position.albumIndex, position.songIndex ?: 0)
        shouldStartPlaying = !model.song.id.equals(this.model?.song?.id)
        setupModel(model)

        if (playIntent != null) {
            activity.unbindService(musicConnection)
            activity.stopService(playIntent)
        }

        playIntent = Intent(activity, MusicService::class.java)
        activity.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
        activity.startService(playIntent)
    }

    fun tryCollapse(): Boolean {
        if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            return false
        } else {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("model", model)
    }

    override fun onDestroy() {
        if (musicService != null) {
            activity.unbindService(musicConnection)
        }
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun setupModel(model: ViewModel) {
        binding.model = model
        this.model = model

        binding.npFastRewind.onClick {
            musicService?.choosePrevious(model.paused)
        }

        binding.npFastForward.onClick {
            musicService?.chooseNext(model.paused)
        }

        binding.npPlayPause.onClick {

            if (model.paused) {
                musicService?.play()
            } else {
                musicService?.pause()
            }

            model.paused = !model.paused
        }

        binding.npSeekBar.onUserSeek { progress ->
            model.timePlayed.set(progress.toLong())
            musicService?.seekTo(progress)
            model.tick()
        }
    }

    private fun startTicking() {
        val musicService = musicService ?: return
        model?.timePlayed?.set(musicService.currentPosition)
        model?.tick()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UPDATE_NOW_PLAYING) {
                model?.albumPosition = intent.getIntExtra("ALBUM_POSITION", 0)
                model?.songPosition = intent.getIntExtra("SONG_POSITION", 0)
                model?.paused = intent.getBooleanExtra("PAUSED", false)
                model?.notifyChange()

                startTicking()
            }
        }
    }

    private val musicConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {

            val service = (binder as MusicService.MusicBinder).service
            musicService = service

            val model = model ?: return
            if (shouldStartPlaying) {

                service.setList(model.albumList)
                service.setArtistName(model.artistName)
                service.setPosition(model.albumPosition, model.songPosition)
                service.playSong()
            }

            model.paused = !service.isPlaying
            startTicking()

        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicService = null
        }
    }

}
