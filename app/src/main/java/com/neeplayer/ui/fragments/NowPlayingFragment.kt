package com.neeplayer.ui.fragments

import android.content.*
import android.databinding.*
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.ui.services.MusicService
import com.neeplayer.databinding.FragmentNowPlayingBinding
import com.neeplayer.model.*
import com.neeplayer.ui.onUserSeek
import com.neeplayer.ui.setCallback
import com.neeplayer.ui.view_models.NowPlayingViewModel
import org.jetbrains.anko.onClick

class NowPlayingFragment : Fragment() {
    companion object{
        val UPDATE_NOW_PLAYING = "UPDATE_NOW_PLAYING"
    }

    private var viewModel: NowPlayingViewModel? = null

    lateinit
    private var binding: FragmentNowPlayingBinding

    lateinit
    private var bottomSheet: BottomSheetBehavior<View>

    private var musicService: MusicService? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentNowPlayingBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<FragmentNowPlayingBinding>(view)
        bottomSheet = BottomSheetBehavior.from(binding.npContainer)

        bottomSheet.setCallback(onSlide = {
            binding.npCollapsed.alpha = it
        })

        binding.npCollapsed.onClick {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViewModel(true)

        val filter = IntentFilter()
        filter.addAction(UPDATE_NOW_PLAYING)
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, filter)
    }

    fun update(shouldStartPlaying: Boolean) {
        setupViewModel(!shouldStartPlaying)
        setupService()
    }

    fun tryCollapse(): Boolean {
        if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            return false
        } else {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }
    }

    override fun onDestroy() {
        if (musicService != null) {
            activity.unbindService(musicConnection)
        }
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun setupViewModel(paused: Boolean) {
        val playlist = Model.nowPlaying ?: return
        val model = NowPlayingViewModel(playlist.artist, playlist.currentAlbum, playlist.currentSong, paused)

        binding.model = model
        this.viewModel = model

        binding.npFastRewind.onClick {
            musicService?.choosePrevious(model.paused)
        }

        binding.npFastForward.onClick {
            musicService?.chooseNext(model.paused)
        }

        val onPlayPauseClick = { view: View? ->
            model.paused = !model.paused
            setupService()
        }

        binding.npPlayPause.onClick(onPlayPauseClick)
        binding.npCollapsedPlayPause.onClick(onPlayPauseClick)

        binding.npSeekBar.onUserSeek { progress ->
            model.timePlayed.set(progress.toLong())
            musicService?.seekTo(progress)
            model.tick()
        }
    }

    private fun startTicking() {
        val musicService = musicService ?: return
        viewModel?.timePlayed?.set(musicService.currentPosition)
        viewModel?.tick()
    }

    private fun setupService() {
        if (musicService == null) {
            val playIntent = Intent(activity, MusicService::class.java)
            activity.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            activity.startService(playIntent)
        } else {
            syncService()
        }
    }

    private fun syncService() {
        val service = musicService ?: return
        val model = viewModel ?: return
        val nowPlaying = Model.nowPlaying ?: return


        if (model.paused) {
            if (service.isPlaying) {
                service.pause()
            }
        } else {
            if (service.trySetPlaylist(nowPlaying)) {
                service.playSong()
                startTicking()
            } else {
                service.play()
            }
        }

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UPDATE_NOW_PLAYING) {
                val viewModel = viewModel ?: return

                viewModel.paused = intent.getBooleanExtra("PAUSED", false)
                viewModel.album.set(Model.nowPlaying?.currentAlbum)
                viewModel.song.set(Model.nowPlaying?.currentSong)

                startTicking()
            }
        }
    }

    private val musicConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            musicService = (binder as MusicService.MusicBinder).service
            syncService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicService = null
        }
    }

}
