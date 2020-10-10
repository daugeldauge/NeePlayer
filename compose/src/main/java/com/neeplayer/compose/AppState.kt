package com.neeplayer.compose

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

data class AppState(
    val currentScreen: Screen = Screen.Artists(),
    val nowPlayingExpanded: Boolean = false,
    val nowPlaying: NowPlayingState? = null,
)

data class NowPlayingState(
    val playlist: List<PlaylistItem>,
    val position: Int,
    val progress: Long,
    val playing: Boolean,
) {
    init {
        require(position in playlist.indices)
    }

    private val current = playlist[position]
    val song = current.song
    val album = current.album
    val artist = current.artist
}

data class PlaylistItem(
    val song: Song,
    val album: Album,
    val artist: Artist
)

sealed class Screen {

    data class Artists(val artists: List<Artist> = emptyList()) : Screen()

    data class Albums(val artist: Artist, val albums: List<AlbumWithSongs> = emptyList()) : Screen()
}


class AppStateContainer {

    val state = MutableStateFlow(AppState())

    fun goBack(): Boolean {
        mutate {
            when {
                nowPlayingExpanded -> copy(nowPlayingExpanded = false)
                currentScreen !is Screen.Artists -> copy(currentScreen = Screen.Artists())
                else -> return false
            }

        }
        return true
    }

    fun goToAlbums(artist: Artist) = mutate {
        copy(currentScreen = Screen.Albums(artist))
    }

    fun playSong(song: Song, artist: Artist, albums: List<AlbumWithSongs>) = mutate {
        val playlist = albums.flatMap { albumWithSongs ->
            albumWithSongs.songs.map {
                PlaylistItem(
                    song = it,
                    album = albumWithSongs.album,
                    artist = artist,
                )
            }
        }

        copy(
            nowPlaying = NowPlayingState(
                playlist = playlist,
                position = playlist.indexOfFirst { song.id == it.song.id }.coerceAtLeast(0),
                progress = 0,
                playing = true,
            )
        )
    }

    fun playOrPause() = mutate {
        if (nowPlaying != null) {
            copy(nowPlaying = nowPlaying.copy(playing = !nowPlaying.playing))
        } else {
            this
        }
    }

    fun playNext() = mutateNowPlaying {
        copy(position = if (position < playlist.lastIndex) position + 1 else 0, progress = 0L)
    }

    fun playPrevious() = mutateNowPlaying {
        copy(position = if (position > 0) position - 1 else playlist.lastIndex, progress = 0L)
    }

    fun seekTo(progress: Long) = mutateNowPlaying {
        copy(progress = progress)
    }

    fun updateArtists(artists: List<Artist>) = mutate {
        val screen = currentScreen
        if (screen is Screen.Artists) {
            copy(currentScreen = screen.copy(artists = artists))
        } else {
            this
        }
    }

    fun updateAlbums(albums: List<AlbumWithSongs>) = mutate {
        val screen = currentScreen
        if (screen is Screen.Albums) {
            copy(currentScreen = screen.copy(albums = albums))
        } else {
            this
        }
    }

    private inline fun mutate(block: AppState.() -> AppState) {
        state.value = state.value.block()
    }

    private fun mutateNowPlaying(block: NowPlayingState.() -> NowPlayingState) = mutate {
        if (nowPlaying != null) {
            copy(nowPlaying = nowPlaying.block()).also { Log.d("tzzz", it.nowPlaying.toString()) }
        } else {
            this
        }
    }
}