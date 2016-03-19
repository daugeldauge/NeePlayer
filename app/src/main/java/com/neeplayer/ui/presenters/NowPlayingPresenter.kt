package com.neeplayer.ui.presenters

import com.neeplayer.model.Model
import com.neeplayer.model.Song
import com.neeplayer.ui.views.NowPlayingView

class NowPlayingPresenter(view: NowPlayingView) : BasePresenter<NowPlayingView>(view) {
    private var lastSong: Song? = null

    private val nowPlayingListener = {
        val song = Model.nowPlaying?.currentSong

        if (song != null && song != lastSong) {
            lastSong = song
            view.setSong(song)
        }

        if (Model.paused) {
            view.pause()
        } else {
            view.play()
        }

    }

    private val progressListener = {
        view.seek(Model.progress)
    }

    init {
        Model.addNowPlayingListener(nowPlayingListener)
        Model.addProgressListener(progressListener)
    }

    fun onPreviousClicked() {
        Model.nowPlaying = Model.nowPlaying?.previous()
    }

    fun onNextClicked() {
        Model.nowPlaying = Model.nowPlaying?.next()
    }

    fun onPlayPauseClicked() {
        Model.paused = !Model.paused
    }

    fun onSeek(progress: Int) {
        Model.progress = progress
    }

    override fun onDestroy() {
        Model.save()
        Model.removeNowPlayingListener(nowPlayingListener)
        Model.removeProgressListener(progressListener)
    }
}