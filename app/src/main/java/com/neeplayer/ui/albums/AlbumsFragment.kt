package com.neeplayer.ui.albums

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.R
import com.neeplayer.di.component
import com.neeplayer.model.AlbumWithSongs
import com.neeplayer.model.Artist
import com.neeplayer.model.Song
import com.neeplayer.ui.common.actionBar
import com.neeplayer.ui.getValue
import com.neeplayer.ui.setValue
import javax.inject.Inject

class AlbumsFragment() : Fragment(), AlbumsView {

    @SuppressLint("ValidFragment")
    constructor(artist: Artist): this() {
        this.artist = artist
    }

    private var artist: Artist by arguments

    @Inject
    internal lateinit var presenter: AlbumsPresenter

    private val listView by lazy {
        view as RecyclerView
    }

    private val adapter by lazy {
        listView.adapter as AlbumSongAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context.component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.layoutManager = LinearLayoutManager(context)

        presenter.bind(this, artist)

        actionBar?.title = artist.name
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.unbind()
    }

    override fun showAlbums(albumsWithSongs: List<AlbumWithSongs>) {
        listView.adapter = AlbumSongAdapter(activity, albumsWithSongs) {
            presenter.onSongClicked(it)
        }
    }

    override fun showNowPlaying(song: Song, paused: Boolean) {
        adapter.updateNowPlaying(song, paused)
    }
}
