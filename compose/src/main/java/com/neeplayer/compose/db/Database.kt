package com.neeplayer.compose.db

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Artists.Albums
import android.provider.MediaStore.Audio.Media
import com.neeplayer.compose.Album
import com.neeplayer.compose.Artist
import com.neeplayer.compose.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Database(private val contentResolver: ContentResolver) {

    suspend fun artists(): List<Artist> {
        return query(
            uri = Artists.EXTERNAL_CONTENT_URI,
            projection = arrayOf(Artists._ID, Artists.ARTIST, Artists.NUMBER_OF_TRACKS, Artists.NUMBER_OF_ALBUMS)
        ) {
            Artist(
                id = id(),
                name = string(Artists.ARTIST) ?: "Unknown",
                numberOfSongs = int(Artists.NUMBER_OF_TRACKS) ?: 0,
                numberOfAlbums = int(Artists.NUMBER_OF_ALBUMS) ?: 0,
                imageUrl = null
            )
        }
    }

    suspend fun albums(artist: Artist): List<Album> {
        @Suppress("DEPRECATION") // Suggested replacement far less convenient :(
        return query(
            uri = Albums.getContentUri("external", artist.id),
            projection = arrayOf(Albums.ALBUM_ID, Albums.ALBUM, Albums.FIRST_YEAR, Albums.ALBUM_ART),
            sortOrder = Albums.FIRST_YEAR
        ) {
            Album(
                id = long(Albums.ALBUM_ID)!!,
                title = string(Albums.ALBUM),
                year = int(Albums.FIRST_YEAR),
                art = string(Albums.ALBUM_ART)?.let { "file://$it" }
            )
        }
    }

    suspend fun songs(album: Album): List<Song> {
        return query(
            uri = Media.EXTERNAL_CONTENT_URI,
            projection = arrayOf(Media._ID, Media.TITLE, MEDIA_DURATION, Media.TRACK),
            selection = "${Media.ALBUM_ID}=${album.id}",
            sortOrder = Media.TRACK
        ) {
            Song(
                id = id(),
                title = string(Media.TITLE),
                duration = long(MEDIA_DURATION) ?: 0,
                track = int(Media.TRACK)
            )
        }
    }


    private suspend fun <T> query(
        uri: Uri,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null,
        mapper: Cursor.() -> T,
    ): List<T> {
        return withContext(Dispatchers.IO) {
            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                ?.use { cursor ->
                    val items = ArrayList<T>(cursor.count)
                    while (cursor.moveToNext()) {
                        items.add(mapper(cursor))
                    }
                    items
                }.orEmpty()
        }
    }

}


private fun Cursor.id(): Long = long(BaseColumns._ID)!!
private fun Cursor.int(column: String): Int? = getInt(getColumnIndexOrThrow(column))
private fun Cursor.long(column: String): Long? = getLong(getColumnIndexOrThrow(column))
private fun Cursor.string(column: String): String? = getString(getColumnIndexOrThrow(column))

@SuppressLint("InlinedApi")
private const val MEDIA_DURATION = Media.DURATION
