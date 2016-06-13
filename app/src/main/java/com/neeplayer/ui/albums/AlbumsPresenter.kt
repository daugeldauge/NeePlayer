package com.neeplayer.ui.albums

import com.neeplayer.model.*
import com.neeplayer.plusAssign
import com.neeplayer.ui.BasePresenter
import javax.inject.Inject

class AlbumsPresenter @Inject constructor(
        private val database: Database, private val model: NowPlayingModel
) : BasePresenter<AlbumsView>() {

    var songs = emptyList<Song>()

    override fun bind(view: AlbumsView) {
        throw UnsupportedOperationException("Please call bind(AlbumsView, Artist) instead")
    }

    fun bind(view: AlbumsView, artist: Artist) {
        super.bind(view)

        val albums = database.getAlbums(artist)

        val albumsWithSongs = albums.map {
            AlbumWithSongs(it, database.getSongs(it))
        }

        songs = albumsWithSongs.flatMap { it.songs }

        view.showAlbums(albumsWithSongs)

        subscriptions += model.nowPlayingObservable.subscribe {
            view.showNowPlaying(it.currentSong, it.paused)
        }

    }

    fun onSongClicked(song: Song) {
        model.nowPlaying = Playlist(songs, songs.indexOf(song), false)
    }
}