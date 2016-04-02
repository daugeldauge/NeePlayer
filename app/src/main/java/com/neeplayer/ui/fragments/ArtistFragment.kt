package com.neeplayer.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.neeplayer.*
import com.neeplayer.model.*
import com.neeplayer.ui.adapters.AlbumSongAdapter
import org.jetbrains.anko.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@FragmentWithArgs
class ArtistFragment : Fragment() {

    @Arg
    lateinit var artist: Artist

    lateinit var albums: List<Album>

    lateinit var albumsWithSongs: List<AlbumWithSongs>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArtistFragmentBuilder.injectArguments(this)
        albums = Database.getAlbums(artist)

        albumsWithSongs = albums.map {
            AlbumWithSongs(it, Database.getSongs(it))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumListView = view as RecyclerView
        val adapter = AlbumSongAdapter(activity, albumsWithSongs)

        albumListView.adapter = adapter
        albumListView.layoutManager = LinearLayoutManager(activity)
    }
}
