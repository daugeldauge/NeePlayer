package com.neeplayer

import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AlbumColumns
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Media
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import java.util.ArrayList

@FragmentWithArgs
class ArtistFragment : Fragment() {

    @Arg
    lateinit var artist: Artist

    lateinit var albumList: ArrayList<Album>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArtistFragmentBuilder.injectArguments(this);
        albumList = getAlbumList(artist.id)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumListView = view as RecyclerView
        val adapter = AlbumSongAdapter(activity, albumList)
        adapter.onSongClickListener = {
            (activity as MainActivity).navigateToNowPlayingFragment(artist.name, albumList, it)
        }

        albumListView.adapter = adapter
        albumListView.layoutManager = LinearLayoutManager(activity)
    }

    private fun getAlbumList(artistId: Long): ArrayList<Album>  {
        val albumList = ArrayList<Album>()
        val uri = Artists.Albums.getContentUri("external", artistId)

        val cursor = activity.contentResolver.query(
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

        val cursor = activity.contentResolver.query(
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
