package com.neeplayer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.concurrent.TimeUnit

abstract class Entity : Serializable {
    abstract val id: Long

    override fun equals(other: Any?): Boolean =
            if (javaClass.isInstance(other)) id == (other as Entity).id
            else false

    override fun hashCode(): Int = id.hashCode()
}

@Parcelize
class Artist(override val id: Long, val name: String, val numberOfSongs: Int, val numberOfAlbums: Int, val imageUrl: String? = null) : Entity(), Parcelable {
    val description: String
    get() = "%d albums, %d songs".format(numberOfAlbums, numberOfSongs)

    fun withImage(imageUrl: String) = Artist(id, name, numberOfSongs, numberOfAlbums, imageUrl)
}

class Album(override val id: Long, val artist: Artist, val title: String?, val year: Int?, val art: String?) : Entity()

class Song(override val id: Long, val album: Album, val title: String?, val duration: Int, val track: Int?) : Entity()

class AlbumWithSongs(val album: Album, val songs: List<Song>) {
        val info: String
        get() = "%d songs, %d min".format(
                songs.size,
                TimeUnit.MILLISECONDS.toMinutes(songs.map { it.duration.toLong() }.sum()))
}

data class Playlist(private val songs: List<Song>, private val index: Int, val paused: Boolean) {

    init {
        if (index < 0 || index >= songs.size) {
            throw IllegalArgumentException("Invalid index of song list")
        }
    }

    val currentSong: Song
        get() = songs[index]

    fun next(): Playlist = copy(index = index.inc() % songs.size)

    fun previous(): Playlist = copy(index = (songs.size + index.dec()) % songs.size)

    fun togglePaused(): Playlist = copy(paused = !paused)
}