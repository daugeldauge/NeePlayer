package com.neeplayer.ui.now_playing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.neeplayer.R
import com.neeplayer.databinding.FragmentNowPlayingBinding
import com.neeplayer.model.Song
import com.neeplayer.ui.CoroFragment
import com.neeplayer.ui.common.*
import org.koin.android.ext.android.inject

class NowPlayingFragment : CoroFragment(R.layout.fragment_now_playing), NowPlayingView {

    @ActionBar.DisplayOptions
    private var oldActionBarDisplayOptions: Int = 0
    private var oldActionBarTitle: CharSequence? = null
    private val ACTION_BAR_THRESHOLD = 0.2
    private var actionBarConfigured = false

    private lateinit var binding: FragmentNowPlayingBinding
    private lateinit var bottomSheet: BottomSheetBehavior<View>

    private val presenter by inject<NowPlayingPresenter>()

    private var freezeProgress = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentNowPlayingBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
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
                actionBar.title = view.context.getString(R.string.now_playing)
            } else if (it >= ACTION_BAR_THRESHOLD && actionBarConfigured) {
                actionBarConfigured = false
                actionBar.displayOptions = oldActionBarDisplayOptions
                actionBar.title = oldActionBarTitle
                actionBar.setHomeAsUpIndicator(null)
            }
        })

        binding.npCollapsed.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.npFastRewind.setOnClickListener { presenter.onPreviousClicked() }
        binding.npFastForward.setOnClickListener { presenter.onNextClicked() }
        binding.npPlayPause.setOnClickListener { presenter.onPlayPauseClicked() }
        binding.npCollapsedPlayPause.setOnClickListener { presenter.onPlayPauseClicked() }

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
        presenter.bind(viewScope, this)
    }

    override fun onDestroyView() {
        presenter.onDestroy()
        super.onDestroyView()
    }

    override fun render(song: Song, paused: Boolean) {
        binding.song = song
        binding.paused = paused
    }

    override fun seek(progress: Int) = uiThread {
        if (!freezeProgress) {
            binding.progress = progress
        }
    }

    fun tryCollapse(): Boolean {
        return if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            false
        } else {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            true
        }
    }

    fun expand() {
        bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    }

}
