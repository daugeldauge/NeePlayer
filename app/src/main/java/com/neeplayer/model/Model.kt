package com.neeplayer.model

import android.content.Context
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Artists.Albums
import android.provider.MediaStore.Audio.Media
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import com.pushtorefresh.storio.contentresolver.queries.Query

object Model {
    private val SHARED_PREFERENCES_NAME = "MAIN"
    private val NOW_PLAYING_KEY = "NOW_PLAYING_PLAYLIST"

    private lateinit var context: Context

    private val prefs by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val storIOContentResolver by lazy {
        DefaultStorIOContentResolver
                .builder()
                .contentResolver(context.contentResolver)
                .build()
    }

    var nowPlaying: Playlist? = null;


    fun init(context: Context) {
        this.context = context.applicationContext
    }

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

    fun getAlbums(artistId: Long): List<Album> {
        return storIOContentResolver
                .get()
                .listOfObjects(Album::class.java)
                .withQuery(Query.builder()
                        .uri(Albums.getContentUri("external", artistId))
                        .columns(BaseColumns._ID, Albums.ALBUM, Albums.FIRST_YEAR, Albums.ALBUM_ART)
                        .sortOrder(Albums.FIRST_YEAR)
                        .build())
                .withGetResolver(AlbumResolver())
                .prepare()
                .executeAsBlocking()
    }

    fun getSongs(albumId: Long): List<Song> {
        return storIOContentResolver
                .get()
                .listOfObjects(Song::class.java)
                .withQuery(Query.builder()
                        .uri(Media.EXTERNAL_CONTENT_URI)
                        .columns(Media._ID, Media.TITLE,  Media.DURATION, Media.TRACK)
                        .where(Media.ALBUM_ID + "=?")
                        .whereArgs(albumId)
                        .build())
                .withGetResolver(SongResolver())
                .prepare()
                .executeAsBlocking()
    }
}