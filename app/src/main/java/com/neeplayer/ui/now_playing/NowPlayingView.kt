package com.neeplayer.ui.now_playing

import com.neeplayer.model.Song

interface NowPlayingView {

    fun render(song: Song, paused: Boolean)

    fun seek(progress: Int)
}