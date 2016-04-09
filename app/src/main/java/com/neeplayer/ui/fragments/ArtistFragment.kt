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
import rx.Subscription
import javax.inject.Inject

@FragmentWithArgs
class ArtistFragment : Fragment() {

    @Arg
    internal lateinit var artist: Artist

    @Inject
    internal lateinit var database: Database

    @Inject
    internal lateinit var model: NowPlayingModel

    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArtistFragmentBuilder.injectArguments(this)
        NeePlayerApp.component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albums = database.getAlbums(artist)

        val albumsWithSongs = albums.map {
            AlbumWithSongs(it, database.getSongs(it))
        }

        val songs = albumsWithSongs.flatMap { it.songs }

        val albumListView = view as RecyclerView
        val adapter = AlbumSongAdapter(activity, albumsWithSongs) {
            model.nowPlaying = Playlist(songs, songs.indexOf(it), false)
        }

        albumListView.adapter = adapter
        albumListView.layoutManager = LinearLayoutManager(activity)

        subscription = model.nowPlayingObservable.subscribe {
            adapter.paused = it.paused
            adapter.nowPlaying = it.currentSong
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscription?.unsubscribe()
    }
}
