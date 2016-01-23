package com.neeplayer

import java.io.Serializable
import java.util.concurrent.TimeUnit

data class Artist(val id: Long, val name: String, val numberOfSongs: Int, val numberOfAlbums: Int, var imageURL: String? = null) {
    val description: String
    get() = "%d albums, %d songs".format(numberOfAlbums, numberOfSongs)
}

data class Album(val id: Long, val title: String?, val year: Int?, val art: String?, val songs: List<Song>) : Serializable {
    val info: String
    get() = "%d songs, %d min".format(
            songs.size,
            TimeUnit.MILLISECONDS.toMinutes(songs.map { it.duration }.sum()))
}

data class Song(val id: Long, val title: String?, val duration: Long, val track: Int?) : Serializable {
    val formattedDuration: String
    get() {
        val min = TimeUnit.MILLISECONDS.toMinutes(duration)
        val sec = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(min)
        return "%d:%02d".format(min, sec)
    }
}

data class Index(val albumIndex: Int, val songIndex: Int? = null)
