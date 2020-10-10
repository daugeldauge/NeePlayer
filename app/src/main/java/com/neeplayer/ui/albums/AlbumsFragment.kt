package com.neeplayer.ui.albums

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neeplayer.R
import com.neeplayer.model.AlbumWithSongs
import com.neeplayer.model.Artist
import com.neeplayer.model.Song
import com.neeplayer.ui.CoroFragment
import com.neeplayer.ui.common.actionBar
import com.neeplayer.ui.getValue
import com.neeplayer.ui.putArgument
import org.koin.android.ext.android.inject

class AlbumsFragment() : CoroFragment(R.layout.fragment_albums), AlbumsView {

    constructor(artist: Artist): this() {
        putArgument(::artist, artist)
    }

    private val artist: Artist by { arguments }

    private val presenter by inject<AlbumsPresenter>()

    private val listView by lazy {
        view as RecyclerView
    }

    private val adapter by lazy {
        listView.adapter as AlbumSongAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.layoutManager = LinearLayoutManager(context)

        presenter.bind(viewScope, this, artist)

        actionBar?.title = artist.name
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun showAlbums(albumsWithSongs: List<AlbumWithSongs>) {
        listView.adapter = AlbumSongAdapter(requireActivity(), albumsWithSongs) {
            presenter.onSongClicked(it)
        }
    }

    override fun showNowPlaying(song: Song, paused: Boolean) {
        adapter.updateNowPlaying(song, paused)
    }
}
