package com.neeplayer.ui.views

import com.neeplayer.model.Artist

interface ArtistsView {
    fun showArtists(artists: List<Artist>)
    fun updateArtist(artist: Artist)
}