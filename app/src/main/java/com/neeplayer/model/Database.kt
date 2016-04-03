package com.neeplayer.model

import android.content.Context
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Artists.Albums
import android.provider.MediaStore.Audio.Media
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import com.pushtorefresh.storio.contentresolver.queries.Query


class Database(context: Context) {

    private val storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(context.contentResolver)
                .build()

    fun getArtists(): List<Artist> {
        return storIOContentResolver
                .get()
                .listOfObjects(Artist::class.java)
                .withQuery(Query.builder()
                        .uri(Artists.EXTERNAL_CONTENT_URI)
                        .columns(Artists._ID, Artists.ARTIST, Artists.NUMBER_OF_TRACKS, Artists.NUMBER_OF_ALBUMS).build())
                .withGetResolver(ArtistResolver())
                .prepare()
                .executeAsBlocking()
    }

    fun getAlbums(artist: Artist): List<Album> {
        return storIOContentResolver
                .get()
                .listOfObjects(Album::class.java)
                .withQuery(Query.builder()
                        .uri(Albums.getContentUri("external", artist.id))
                        .columns(BaseColumns._ID, Albums.ALBUM, Albums.FIRST_YEAR, Albums.ALBUM_ART)
                        .sortOrder(Albums.FIRST_YEAR)
                        .build())
                .withGetResolver(AlbumResolver(artist))
                .prepare()
                .executeAsBlocking()
    }

    fun getSongs(album: Album): List<Song> {
        return storIOContentResolver
                .get()
                .listOfObjects(Song::class.java)
                .withQuery(Query.builder()
                        .uri(Media.EXTERNAL_CONTENT_URI)
                        .columns(Media._ID, Media.TITLE,  Media.DURATION, Media.TRACK)
                        .where(Media.ALBUM_ID + "=?")
                        .whereArgs(album.id)
                        .sortOrder(Media.TRACK)
                        .build())
                .withGetResolver(SongResolver(album))
                .prepare()
                .executeAsBlocking()
    }

    fun restorePlaylist(songId: Long): Playlist? {
        val artistId = getArtistId(songId) ?: return null
        val artist = getArtist(artistId) ?: return null

        val albums = getAlbums(artist)
        val songs = albums.flatMap { getSongs(it) }

        val index = songs.indexOfFirst { it.id == songId }

        return if (index < 0) null
               else Playlist(songs, index)
    }

    fun getArtist(artistId: Long): Artist? {
        return storIOContentResolver
                .get()
                .`object`(Artist::class.java)
                .withQuery(Query.builder()
                        .uri(Artists.EXTERNAL_CONTENT_URI)
                        .columns(Artists._ID, Artists.ARTIST, Artists.NUMBER_OF_TRACKS, Artists.NUMBER_OF_ALBUMS)
                        .where(Artists._ID + "=?")
                        .whereArgs(artistId)
                        .build())
                .withGetResolver(ArtistResolver())
                .prepare()
                .executeAsBlocking()
    }

    fun getArtistId(songId: Long): Long? {
        val cursor = storIOContentResolver
                .get()
                .cursor()
                .withQuery(Query.builder()
                        .uri(Media.EXTERNAL_CONTENT_URI)
                        .columns(Media.ARTIST_ID)
                        .where(Media._ID + "=?")
                        .whereArgs(songId)
                        .build())
                .prepare()
                .executeAsBlocking()

        val artistId = if (cursor.moveToFirst()) {
            cursor.getLong(Media.ARTIST_ID)
        } else {
            null
        }

        cursor.close()
        return artistId
    }
}