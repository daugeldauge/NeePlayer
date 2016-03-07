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
import com.neeplayer.R
import com.neeplayer.model.Album
import com.neeplayer.model.Artist
import com.neeplayer.model.Model
import com.neeplayer.ui.activities.MainActivity
import com.neeplayer.ui.adapters.AlbumSongAdapter

@FragmentWithArgs
class ArtistFragment : Fragment() {

    @Arg
    lateinit var artist: Artist

    lateinit var albumList: List<Album>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArtistFragmentBuilder.injectArguments(this);
        albumList = Model.getAlbums(artist.id)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumListView = view as RecyclerView
        val adapter = AlbumSongAdapter(activity, albumList)
        adapter.onSongClickListener = {
            (activity as MainActivity).navigateToNowPlayingFragment(artist, albumList, it)
        }

        albumListView.adapter = adapter
        albumListView.layoutManager = LinearLayoutManager(activity)
    }
}
