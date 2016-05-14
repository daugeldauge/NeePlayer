package com.neeplayer.ui.views.impl

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.NeePlayerApp
import com.neeplayer.databinding.FragmentNowPlayingBinding
import com.neeplayer.model.Song
import com.neeplayer.ui.onUserSeek
import com.neeplayer.ui.presenters.NowPlayingPresenter
import com.neeplayer.ui.setCallback
import com.neeplayer.ui.uiThread
import com.neeplayer.ui.views.NowPlayingView
import org.jetbrains.anko.onClick
import javax.inject.Inject

class NowPlayingFragment : Fragment(), NowPlayingView {

    private val handler = Handler()
    private var needToStopTicking = false
    private val TICK_PERIOD = 1000
    private var overallTicking = 0

    lateinit
    private var binding: FragmentNowPlayingBinding

    lateinit
    private var bottomSheet: BottomSheetBehavior<View>

    @Inject
    lateinit var presenter: NowPlayingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NeePlayerApp.component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentNowPlayingBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<FragmentNowPlayingBinding>(view)
        bottomSheet = BottomSheetBehavior.from(binding.npContainer)

        bottomSheet.setCallback(onSlide = {
            binding.npCollapsed.alpha = it
        })

        binding.npCollapsed.onClick {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.npFastRewind.onClick         { presenter.onPreviousClicked() }
        binding.npFastForward.onClick        { presenter.onNextClicked() }
        binding.npPlayPause.onClick          { presenter.onPlayPauseClicked() }
        binding.npCollapsedPlayPause.onClick { presenter.onPlayPauseClicked() }

        binding.npSeekBar.onUserSeek(
                onProgress = { binding.progress = it },
                onTouchStopped = { presenter.onSeek(binding.progress) }
        )
        presenter.bind(this)
    }

    override fun onDestroyView() {
        handler.removeCallbacks(tock)
        presenter.unbind()
        super.onDestroyView()
    }

    override fun setSong(song: Song) = uiThread {
        binding.song = song
        overallTicking = 0
    }

    override fun play() = uiThread {
        binding.paused = false
        tick()
    }

    override fun pause() = uiThread {
        binding.paused = true
        needToStopTicking = true
    }

    override fun seek(progress: Int) = uiThread {
        binding.progress = progress
        if (binding.song != null) {
            tick()
        }
    }

    fun tryCollapse(): Boolean {
        if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            return false
        } else {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }
    }

    private fun tick() {
        if (binding.paused) {
            return
        }

        needToStopTicking = false
        val delay = TICK_PERIOD - (binding.progress % TICK_PERIOD)
        handler.removeCallbacks(tock)
        handler.postDelayed(tock, delay.toLong())
    }

    private val tock = Runnable {
        if (!needToStopTicking) {
            overallTicking += TICK_PERIOD
            presenter.onTick(overallTicking)
            binding.progress = Math.min(binding.song.duration, TICK_PERIOD * (binding.progress / TICK_PERIOD).inc())
            tick()
        }
    }

}