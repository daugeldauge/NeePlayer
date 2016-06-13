package com.neeplayer.ui.now_playing

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.NeePlayerApp
import com.neeplayer.R
import com.neeplayer.databinding.FragmentNowPlayingBinding
import com.neeplayer.model.Song
import com.neeplayer.ui.common.*
import org.jetbrains.anko.onClick
import javax.inject.Inject

class NowPlayingFragment : Fragment(), NowPlayingView {

    @ActionBar.DisplayOptions
    private var oldActionBarDisplayOptions: Int = 0
    private var oldActionBarTitle: CharSequence? = null
    private val ACTION_BAR_THRESHOLD = 0.2
    private var actionBarConfigured = false

    lateinit
    private var binding: FragmentNowPlayingBinding

    lateinit
    private var bottomSheet: BottomSheetBehavior<View>

    @Inject
    lateinit var presenter: NowPlayingPresenter

    private var freezeProgress = false

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

        view.onFirstLayout {
            binding.npSeekBar.translationY = binding.npAlbumArt.measuredHeight - binding.npSeekBar.measuredHeight / 2f
        }

        bottomSheet.setCallback(onSlide = {
            binding.npCollapsed.alpha = it
            val actionBar = actionBar ?: return@setCallback

            if (it < ACTION_BAR_THRESHOLD && !actionBarConfigured) {
                actionBarConfigured = true
                oldActionBarDisplayOptions = actionBar.displayOptions
                oldActionBarTitle = actionBar.title
                actionBar.setDisplayShowHomeEnabled(true)
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
                actionBar.title = context.getString(R.string.now_playing)
            } else if (it >= ACTION_BAR_THRESHOLD && actionBarConfigured) {
                actionBarConfigured = false
                actionBar.displayOptions = oldActionBarDisplayOptions
                actionBar.title = oldActionBarTitle
                actionBar.setHomeAsUpIndicator(null)
            }
        })

        binding.npCollapsed.onClick {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.npFastRewind.onClick         { presenter.onPreviousClicked() }
        binding.npFastForward.onClick        { presenter.onNextClicked() }
        binding.npPlayPause.onClick          { presenter.onPlayPauseClicked() }
        binding.npCollapsedPlayPause.onClick { presenter.onPlayPauseClicked() }

        binding.npSeekBar.onUserSeek(
                onProgress = {
                    freezeProgress = true
                    binding.progress = it
                },
                onTouchStopped = {
                    freezeProgress = false
                    presenter.onSeek(binding.progress)
                }
        )
        presenter.bind(this)
    }

    override fun onDestroyView() {
        presenter.unbind()
        super.onDestroyView()
    }

    override fun setSong(song: Song) = uiThread {
        binding.song = song
    }

    override fun play() = uiThread {
        binding.paused = false
    }

    override fun pause() = uiThread {
        binding.paused = true
    }

    override fun seek(progress: Int) = uiThread {
        if (!freezeProgress) {
            binding.progress = progress
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

}
