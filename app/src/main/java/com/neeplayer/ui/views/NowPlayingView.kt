package com.neeplayer.ui.views

import com.neeplayer.model.Song

interface NowPlayingView {

    fun setSong(song: Song)

    fun play()

    fun pause()

    fun seek(progress: Int)

}