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

data class Index(val albumIndex: Int, val songIndex: Int? = null) : Serializable

data class Playlist(val artist: Artist, val albumList: List<Album>, val song: Song)