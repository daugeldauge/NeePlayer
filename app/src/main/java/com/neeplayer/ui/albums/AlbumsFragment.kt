package com.neeplayer.ui.albums

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.neeplayer.NeePlayerApp
import com.neeplayer.R
import com.neeplayer.model.*
import com.neeplayer.ui.common.actionBar
import rx.Subscription
import javax.inject.Inject

@FragmentWithArgs
class AlbumsFragment : Fragment(), AlbumsView {

    @Arg
    internal lateinit var artist: Artist

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
        AlbumsFragmentBuilder.injectArguments(this)
        NeePlayerApp.component.inject(this)
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
