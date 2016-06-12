package com.neeplayer.ui.now_playing

import com.neeplayer.minutes
import com.neeplayer.model.NowPlayingModel
import com.neeplayer.model.Song
import com.neeplayer.plusAssign
import com.neeplayer.seconds
import com.neeplayer.ui.BasePresenter
import javax.inject.Inject

class NowPlayingPresenter @Inject constructor(private val model: NowPlayingModel) : BasePresenter<NowPlayingView>() {

    private var lastSong: Song? = null

    override fun bind(view: NowPlayingView) {
        subscriptions += model.nowPlayingObservable.subscribe {
            val song = it.currentSong

            if (song != lastSong) {
                lastSong = song
                isCurrentSongScrobbled = false
                view.setSong(song)
            }

            if (it.paused) {
                view.pause()
            } else {
                view.play()
            }
        }

        subscriptions += model.progressObservable.subscribe {
            view.seek(model.progress)
        }
    }

    fun onPreviousClicked() {
        model.nowPlaying = model.nowPlaying?.previous()
    }

    fun onNextClicked() {
        model.nowPlaying = model.nowPlaying?.next()
    }

    fun onPlayPauseClicked() {
        model.nowPlaying = model.nowPlaying?.togglePaused()
    }

    fun onSeek(progress: Int) {
        model.progress = progress
    }

    private val minSongLengthToScrobble = 30.seconds
    private val scrobbleFractionThreshold = 0.5
    private val scrobbleThreshold = 4.minutes

    private var isCurrentSongScrobbled = false

    fun onTick(ticking: Int) {
        val song = lastSong
        if (song == null || isCurrentSongScrobbled) {
            return
        }

        if (song.duration > minSongLengthToScrobble && (ticking >= song.duration * scrobbleFractionThreshold || ticking >= scrobbleThreshold )) {
            isCurrentSongScrobbled = true
            model.scrobble(song)
        }
    }

    override fun unbind() {
        model.save()
        super.unbind()
    }
}