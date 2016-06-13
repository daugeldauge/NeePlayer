package com.neeplayer.ui.now_playing

import com.neeplayer.minutes
import com.neeplayer.model.NowPlayingService
import com.neeplayer.model.Song
import com.neeplayer.plusAssign
import com.neeplayer.seconds
import com.neeplayer.ui.BasePresenter
import javax.inject.Inject

class NowPlayingPresenter @Inject constructor(private val service: NowPlayingService) : BasePresenter<NowPlayingView>() {

    private var lastSong: Song? = null
    private var lastSetProgress: Int? = null

    override fun bind(view: NowPlayingView) {
        super.bind(view)
        subscriptions += service.nowPlayingObservable.subscribe {
            it ?: return@subscribe

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

        subscriptions += service.progressObservable.subscribe {
            if (it != lastSetProgress) {
                view.seek(it)
            }
        }
    }

    fun onPreviousClicked() {
        service.nowPlaying = service.nowPlaying?.previous()
    }

    fun onNextClicked() {
        service.nowPlaying = service.nowPlaying?.next()
    }

    fun onPlayPauseClicked() {
        service.nowPlaying = service.nowPlaying?.togglePaused()
    }

    fun onSeek(progress: Int) {
        lastSetProgress = progress
        service.progress = progress
    }

    override fun unbind() {
        service.save()
        super.unbind()
    }
}