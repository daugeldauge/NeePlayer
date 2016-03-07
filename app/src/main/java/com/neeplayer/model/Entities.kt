package com.neeplayer.model

import java.io.Serializable
import java.util.concurrent.TimeUnit

data class Artist(val id: Long, val name: String, val numberOfSongs: Int, val numberOfAlbums: Int, var imageURL: String? = null) : Serializable {
    val description: String
    get() = "%d albums, %d songs".format(numberOfAlbums, numberOfSongs)
}

data class Album(val id: Long, val title: String?, val year: Int?, val art: String?, val songs: List<Song>) : Serializable {
    val info: String
    get() = "%d songs, %d min".format(
            songs.size,
            TimeUnit.MILLISECONDS.toMinutes(songs.map { it.duration }.sum()))
}

data class Song(val id: Long, val title: String?, val duration: Long, val track: Int?) : Serializable

sealed class Index {
    class Album(val value: Int) : Index()
    class Song(val albumIndex: Int, val songIndex: Int) : Index(), Serializable
}

data class Playlist(val artist: Artist, val albumList: List<Album>, val currentPosition: Index.Song): Serializable {

    init {
        // Checks that currentPosition lays in the right bounds
        currentSong
    }

    val currentSong: Song
        get() = currentAlbum.songs[currentPosition.songIndex]

    val currentAlbum: Album
        get() = albumList[currentPosition.albumIndex]


    fun next(): Playlist {
        val albumIndex = currentPosition.albumIndex
        val songIndex = currentPosition.songIndex

        return copy(currentPosition =
            if (songIndex.inc() < currentAlbum.songs.size) {
                Index.Song(albumIndex, songIndex.inc())
            } else if (albumIndex.inc() < albumList.size) {
                Index.Song(albumIndex.inc(), 0)
            } else {
                Index.Song(0, 0)
            }
        )
    }

    fun previous(): Playlist {
        val albumIndex = currentPosition.albumIndex
        val songIndex = currentPosition.songIndex

        return copy(currentPosition =
            if (songIndex.dec() >= 0) {
                Index.Song(albumIndex, songIndex.dec())
            } else if (albumIndex.dec() >= 0) {
                Index.Song(albumIndex.dec(), albumList[albumIndex.dec()].songs.lastIndex)
            } else {
                Index.Song(albumList.lastIndex, albumList[albumList.lastIndex].songs.lastIndex)
            }
        )
    }

}