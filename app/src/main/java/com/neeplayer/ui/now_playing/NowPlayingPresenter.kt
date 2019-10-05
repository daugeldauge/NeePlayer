package com.neeplayer.ui.now_playing

import com.neeplayer.model.NowPlayingService
import com.neeplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class NowPlayingPresenter @Inject constructor(private val service: NowPlayingService) {

    private var lastSong: Song? = null
    private var lastSetProgress: Int? = null

    fun bind(scope: CoroutineScope, view: NowPlayingView) {
        scope.launch {
            service.nowPlayingFlow.collect {
                val song = it.currentSong

                if (song != lastSong) {
                    lastSong = song
                    view.setSong(song)
                }

                if (it.paused) {
                    view.pause()
                } else {
                    view.play()
                }
            }
        }

        scope.launch {
            service.progressFlow.collect {
                if (it != lastSetProgress) {
                    view.seek(it)
                }
            }
        }
    }

    fun onPreviousClicked() {
        service.alter { current -> current?.previous() }
    }

    fun onNextClicked() {
        service.alter { current -> current?.next() }
    }

    fun onPlayPauseClicked() {
        service.alter { current -> current?.togglePaused() }
    }

    fun onSeek(progress: Int) {
        lastSetProgress = progress
        service.offerProgress(progress)
    }

    fun onDestroy() {
        service.save()
    }
}