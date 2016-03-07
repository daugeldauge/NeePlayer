package com.neeplayer.model

import android.content.Context
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Artists.Albums
import android.provider.MediaStore.Audio.Media
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import com.pushtorefresh.storio.contentresolver.queries.Query
import org.jetbrains.anko.collections.forEachWithIndex

// TODO: Use DI instead of global state
object Model {
    private val SHARED_PREFERENCES_NAME = "MAIN"
    private val NOW_PLAYING_SONG = "NOW_PLAYING_PLAYSONG"

    private lateinit var context: Context

    private val prefs by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var nowPlaying: Playlist? = null
    set(value) {
        field = value
        onNowPlayingChangeListeners.forEach { it() }
    }

    // TODO: Fix memory leak
    val onNowPlayingChangeListeners: MutableSet<(() -> Unit)> = mutableSetOf()

    private val storIOContentResolver by lazy {
        DefaultStorIOContentResolver
                .builder()
                .contentResolver(context.contentResolver)
                .build()
    }

    fun init(context: Context) {
        this.context = context.applicationContext

        val nowPlayingSongId = prefs.getLong(NOW_PLAYING_SONG, -1L)
        if (nowPlayingSongId == -1L) {
            return
        }

        val artistId = getArtistId(nowPlayingSongId) ?: return
        val artist = getArtist(artistId) ?: return
        val albums = getAlbums(artistId)

        albums.forEachWithIndex {
            albumIndex, album -> album.songs.forEachWithIndex {
                songIndex, song -> if (song.id == nowPlayingSongId) {
                    nowPlaying = Playlist(artist, albums, Index.Song(albumIndex, songIndex))
                    return
                }
            }
        }

    }

    fun save() {
        val nowPlaying = nowPlaying ?: return
        prefs.edit().putLong(NOW_PLAYING_SONG, nowPlaying.currentSong.id).apply()
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
                        .sortOrder(Media.TRACK)
                        .build())
                .withGetResolver(SongResolver())
                .prepare()
                .executeAsBlocking()
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