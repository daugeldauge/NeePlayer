package com.neeplayer.ui.albums

import com.neeplayer.model.AlbumWithSongs
import com.neeplayer.model.Song

interface AlbumsView {
    fun showAlbums(albumsWithSongs: List<AlbumWithSongs>)
    fun showNowPlaying(song: Song, paused: Boolean)
}