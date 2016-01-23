package com.neeplayer

import android.app.Activity
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AlbumColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Media
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.activity_artist.album_list
import org.jetbrains.anko.startActivity
import java.io.Serializable
import java.util.ArrayList

class ArtistActivity : Activity() {

    var albumList: List<Album>? = null
    var artistName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist)

        artistName = intent.getStringExtra("ARTIST_NAME")
        val artistId = intent.getLongExtra("ARTIST_ID", -1)

        title = artistName

        albumList = getAlbumList(artistId)

        val adapter = AlbumSongAdapter(this, albumList as List<Album>)
        adapter.onSongClickListener = { index ->
            startActivity<NowPlayingActivity>(
                    "ALBUM_POSITION" to index.albumIndex,
                    "SONG_POSITION" to index.songIndex!!,
                    "ALBUM_LIST" to albumList as Serializable,
                    "ARTIST_NAME" to artistName as String
            )
        }

        album_list.adapter = adapter
        album_list.layoutManager = LinearLayoutManager(this)
    }

    private fun getAlbumList(artistId: Long): List<Album>  {
        val albumList = ArrayList<Album>()
        val uri = Artists.Albums.getContentUri("external", artistId)

        val cursor = contentResolver.query(
                uri,
                arrayOf(BaseColumns._ID, AlbumColumns.ALBUM, AlbumColumns.FIRST_YEAR, AlbumColumns.ALBUM_ART),
                null,
                null,
                AlbumColumns.FIRST_YEAR)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(0)

                val album = Album(
                        id,
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        getAlbumSongs(id))

                albumList.add(album)
            } while (cursor.moveToNext())

            cursor.close()
        }

        return albumList
    }

    private fun getAlbumSongs(albumId: Long): List<Song> {
        val list = ArrayList<Song>()

        val cursor = contentResolver.query(
                Media.EXTERNAL_CONTENT_URI,
                arrayOf(BaseColumns._ID, MediaStore.MediaColumns.TITLE,  AudioColumns.DURATION, AudioColumns.TRACK),
                AudioColumns.ALBUM_ID + "=?",
                arrayOf(albumId.toString()),
                AudioColumns.TRACK)


        if (cursor != null && cursor.moveToFirst()) {

            do {
                list.add(Song(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getInt(3)))
            } while (cursor.moveToNext())

            cursor.close()
        }

        return list
    }
}
