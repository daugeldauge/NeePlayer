package com.neeplayer.model

import java.io.Serializable
import java.util.concurrent.TimeUnit

open class Entity(val id: Long) : Serializable {
    override fun equals(other: Any?): Boolean =
            if (javaClass.isInstance(other)) id == (other as Entity).id
            else false

    override fun hashCode(): Int = id.hashCode()
}

class Artist(id: Long, val name: String, val numberOfSongs: Int, val numberOfAlbums: Int) : Entity(id), Serializable {
    val description: String
    get() = "%d albums, %d songs".format(numberOfAlbums, numberOfSongs)
}

class Album(id: Long, val artist: Artist, val title: String?, val year: Int?, val art: String?) : Entity(id)

class Song(id: Long, val album: Album, val title: String?, val duration: Int, val track: Int?) : Entity(id)

class AlbumWithSongs(val album: Album, val songs: List<Song>) {
        val info: String
        get() = "%d songs, %d min".format(
                songs.size,
                TimeUnit.MILLISECONDS.toMinutes(songs.map { it.duration.toLong() }.sum()))
}

sealed class Index {
    class Album(val value: Int) : Index()
    class Song(val albumIndex: Int, val songIndex: Int) : Index(), Serializable
}

data class Playlist(private val songs: List<Song>, private val index: Int) {

    init {
        if (index < 0 || index >= songs.size) {
            throw IllegalArgumentException("Invalid index of song list")
        }
    }

    val currentSong: Song
        get() = songs[index]

    fun next(): Playlist = Playlist(songs, index.inc() % songs.size)

    fun previous(): Playlist = Playlist(songs, (songs.size + index.dec()) % songs.size)

}