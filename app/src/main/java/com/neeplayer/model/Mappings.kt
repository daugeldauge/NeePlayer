package com.neeplayer.model

import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Artists.Albums
import android.provider.MediaStore.Audio.Media
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver


fun Cursor.getInt(column: String)    : Int?    = getInt   (getColumnIndexOrThrow(column))
fun Cursor.getLong(column: String)   : Long?   = getLong  (getColumnIndexOrThrow(column))
fun Cursor.getString(column: String) : String? = getString(getColumnIndexOrThrow(column))

class ArtistResolver : DefaultGetResolver<Artist>() {
    override fun mapFromCursor(cursor: Cursor): Artist {
        return Artist(
            id             = cursor.getLong(   Artists._ID)!!,
            name           = cursor.getString( Artists.ARTIST)!!,
            numberOfSongs  = cursor.getInt(    Artists.NUMBER_OF_TRACKS)!!,
            numberOfAlbums = cursor.getInt(    Artists.NUMBER_OF_ALBUMS)!!
        )
    }
}

class AlbumResolver : DefaultGetResolver<Album>() {
    override fun mapFromCursor(cursor: Cursor): Album {
        val id = cursor.getLong(BaseColumns._ID)!!
        return Album(
                id    = id,
                title = cursor.getString( Albums.ALBUM),
                year  = cursor.getInt(    Albums.FIRST_YEAR),
                art   = cursor.getString( Albums.ALBUM_ART),
                songs = Model.getSongs(   id)
        )
    }
}

class SongResolver : DefaultGetResolver<Song>() {
    override fun mapFromCursor(cursor: Cursor): Song {
        return Song(
                id       = cursor.getLong(   Media._ID)!!,
                title    = cursor.getString( Media.TITLE),
                duration = cursor.getLong(   Media.DURATION)!!,
                track    = cursor.getInt(    Media.TRACK)
        )
    }
}