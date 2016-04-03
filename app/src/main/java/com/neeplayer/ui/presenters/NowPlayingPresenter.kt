package com.neeplayer.ui.presenters

import com.neeplayer.NeePlayerApp
import com.neeplayer.model.NowPlayingModel
import com.neeplayer.model.Song
import com.neeplayer.ui.views.NowPlayingView
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NowPlayingPresenter(view: NowPlayingView) : BasePresenter<NowPlayingView>(view) {

    private var lastSong: Song? = null

    private val nowPlayingListener = {
        val song = model.nowPlaying?.currentSong

        if (song != null && song != lastSong) {
            lastSong = song
            isCurrentSongScrobbled = false
            view.setSong(song)
        }

        if (model.paused) {
            view.pause()
        } else {
            view.play()
        }

    }

    private val progressListener = {
        view.seek(model.progress)
    }

    @Inject
    internal lateinit var model: NowPlayingModel

    init {
        NeePlayerApp.component!!.inject(this)
        model.addNowPlayingListener(nowPlayingListener)
        model.addProgressListener(progressListener)
    }

    fun onPreviousClicked() {
        model.nowPlaying = model.nowPlaying?.previous()
    }

    fun onNextClicked() {
        model.nowPlaying = model.nowPlaying?.next()
    }

    fun onPlayPauseClicked() {
        model.paused = !model.paused
    }

    fun onSeek(progress: Int) {
        model.progress = progress
    }

    val minSongLengthToScrobble = TimeUnit.SECONDS.toMillis(30)
    val scrobbleFractionThreshold = 0.5
    val scrobbleThreshold = TimeUnit.MINUTES.toMillis(4)

    var isCurrentSongScrobbled = false

    fun onTick(ticking: Int) {
        val song = lastSong
        if (song == null || isCurrentSongScrobbled) {
            return
        }

        if (song.duration > minSongLengthToScrobble && (ticking >= song.duration * scrobbleFractionThreshold || ticking >= scrobbleThreshold )) {
            isCurrentSongScrobbled = true
            model.scrobble()
        }
    }

    override fun onDestroy() {
        model.save()
        model.removeNowPlayingListener(nowPlayingListener)
        model.removeProgressListener(progressListener)
    }
}