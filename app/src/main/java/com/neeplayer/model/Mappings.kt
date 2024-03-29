package com.neeplayer.model

import android.database.Cursor
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Artists.Albums
import android.provider.MediaStore.Audio.Media
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver


fun Cursor.getInt(column: String): Int? = getInt(getColumnIndexOrThrow(column))
fun Cursor.getLong(column: String): Long? = getLong(getColumnIndexOrThrow(column))
fun Cursor.getString(column: String): String? = getString(getColumnIndexOrThrow(column))

class ArtistResolver(private val artistImagesStorage: ArtistImagesStorage) : DefaultGetResolver<Artist>() {
    override fun mapFromCursor(cursor: Cursor): Artist {
        val name = cursor.getString(Artists.ARTIST).orEmpty()
        return Artist(
            id = cursor.getLong(Artists._ID) ?: 0,
            name = name,
            numberOfSongs = cursor.getInt(Artists.NUMBER_OF_TRACKS) ?: 0,
            numberOfAlbums = cursor.getInt(Artists.NUMBER_OF_ALBUMS) ?: 0,
            imageUrl = artistImagesStorage.get(name),
        )
    }
}

class AlbumResolver(val artist: Artist) : DefaultGetResolver<Album>() {
    override fun mapFromCursor(cursor: Cursor): Album {
        val id = cursor.getLong(Albums.ALBUM_ID)!!
        return Album(
            artist = artist,
            id = id,
            title = cursor.getString(Albums.ALBUM),
            year = cursor.getInt(Albums.FIRST_YEAR),
            art = "neeplayer://album/$id",
        )
    }
}

class SongResolver(val album: Album) : DefaultGetResolver<Song>() {
    override fun mapFromCursor(cursor: Cursor): Song {
        return Song(
            album = album,
            id = cursor.getLong(Media._ID)!!,
            title = cursor.getString(Media.TITLE),
            duration = cursor.getInt(Media.DURATION) ?: 0,
            track = cursor.getInt(Media.TRACK),
        )
    }
}
