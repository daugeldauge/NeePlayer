package com.neeplayer.ui.view_models

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import android.databinding.ObservableLong
import android.os.Handler
import com.neeplayer.BR
import com.neeplayer.model.Album
import com.neeplayer.model.Artist
import com.neeplayer.model.Song

class NowPlayingViewModel(artist: Artist, album: Album, song: Song, paused: Boolean) : BaseObservable(){

    private val handler = Handler()
    private var needToStopTicking = false
    private val TICK_PERIOD = 1000

    private val tock = Runnable {
        if (!needToStopTicking) {
            timePlayed.set(Math.min(song.duration, TICK_PERIOD * (timePlayed.get() / TICK_PERIOD).inc()))
            tick()
        }
    }

    @Bindable
    var paused = paused
        set(value) {
            field = value
            if (value) {
                needToStopTicking = true
            }
            notifyPropertyChanged(BR.paused)
        }

    var artist = ObservableField(artist)
    val album = ObservableField(album)
    val song = ObservableField(song)
    val timePlayed = ObservableLong(0)

    fun tick() {
        if (paused) {
            return
        }

        needToStopTicking = false
        val delay = TICK_PERIOD - (timePlayed.get() % TICK_PERIOD)
        handler.removeCallbacks(tock)
        handler.postDelayed(tock, delay)
    }
}