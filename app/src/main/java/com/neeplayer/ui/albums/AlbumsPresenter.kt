package com.neeplayer.ui.albums

import com.neeplayer.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AlbumsPresenter(
        private val database: Database,
        private val nowPlayingService: NowPlayingService
) {

    private var songs = emptyList<Song>()

    fun bind(scope: CoroutineScope, view: AlbumsView, artist: Artist) {
        val albums = database.getAlbums(artist)

        val albumsWithSongs = albums.map {
            AlbumWithSongs(it, database.getSongs(it))
        }

        songs = albumsWithSongs.flatMap { it.songs }

        view.showAlbums(albumsWithSongs)

        scope.launch {
            nowPlayingService.nowPlayingFlow.collect {
                view.showNowPlaying(it.currentSong, it.paused)
            }
        }
    }

    fun onSongClicked(song: Song) {
        nowPlayingService.alter { Playlist(songs, songs.indexOf(song), false) }
    }
}