package com.neeplayer.model

import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Artists.Albums
import android.provider.MediaStore.Audio.Media
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver
import javax.inject.Inject


fun Cursor.getInt(column: String)    : Int?    = getInt   (getColumnIndexOrThrow(column))
fun Cursor.getLong(column: String)   : Long?   = getLong  (getColumnIndexOrThrow(column))
fun Cursor.getString(column: String) : String? = getString(getColumnIndexOrThrow(column))

class ArtistResolver @Inject constructor (private val artistImagesStorage: ArtistImagesStorage) : DefaultGetResolver<Artist>() {
    override fun mapFromCursor(cursor: Cursor): Artist {
        val name = cursor.getString(Artists.ARTIST)!!
        return Artist(
            id             = cursor.getLong(Artists._ID)!!,
            name           = name,
            numberOfSongs  = cursor.getInt(Artists.NUMBER_OF_TRACKS)!!,
            numberOfAlbums = cursor.getInt(Artists.NUMBER_OF_ALBUMS)!!,
            imageUrl       = artistImagesStorage.get(name)
        )
    }
}

class AlbumResolver(val artist: Artist) : DefaultGetResolver<Album>() {
    override fun mapFromCursor(cursor: Cursor): Album {
        @Suppress("DEPRECATION") // Suggested replacement far less convenient :(
        return Album(
                artist = artist,
                id     = cursor.getLong(BaseColumns._ID)!!,
                title  = cursor.getString( Albums.ALBUM),
                year   = cursor.getInt(    Albums.FIRST_YEAR),
                art    = cursor.getString( Albums.ALBUM_ART)
        )
    }
}

class SongResolver(val album: Album) : DefaultGetResolver<Song>() {
    override fun mapFromCursor(cursor: Cursor): Song {
        return Song(
                album    = album,
                id       = cursor.getLong(   Media._ID)!!,
                title    = cursor.getString( Media.TITLE),
                duration = cursor.getInt(    Media.DURATION)!!,
                track    = cursor.getInt(    Media.TRACK)
        )
    }
}