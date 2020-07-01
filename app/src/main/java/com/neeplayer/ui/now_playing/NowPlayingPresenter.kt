package com.neeplayer.ui.now_playing

import com.neeplayer.model.NowPlayingService
import com.neeplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NowPlayingPresenter(private val service: NowPlayingService) {

    private val progressPayload
        get() = hashCode()

    fun bind(scope: CoroutineScope, view: NowPlayingView) {
        scope.launch {
            service.nowPlayingFlow.collect {
                view.render(it.currentSong, it.paused)
            }
        }

        scope.launch {
            service.progressFlow.collect { (payload, value) ->
                if (payload != progressPayload) {
                    view.seek(value)
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

    fun onPauseClicked() {
        service.alter { current -> current?.copy(paused = true) }
    }

    fun onSeek(progress: Int) {
        service.offerProgress(progressPayload, progress)
    }

    fun onDestroy() {
        service.save()
    }
}