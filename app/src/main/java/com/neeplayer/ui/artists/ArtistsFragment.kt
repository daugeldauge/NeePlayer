package com.neeplayer.ui.artists

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.NeePlayerApp
import com.neeplayer.R
import com.neeplayer.model.Artist
import com.neeplayer.ui.common.actionBar
import com.neeplayer.ui.MainActivity
import com.neeplayer.ui.artists.ArtistAdapter
import com.neeplayer.ui.artists.ArtistsPresenter
import com.neeplayer.ui.common.uiThread
import com.neeplayer.ui.artists.ArtistsView
import javax.inject.Inject

class ArtistsFragment : Fragment(), ArtistsView {
    companion object {
        val TAG = ArtistsFragment::class.java.name;
    }

    @Inject
    lateinit internal var presenter: ArtistsPresenter

    private val adapter by lazy {
        ArtistAdapter(activity) {
            presenter.onArtistClicked(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view as RecyclerView

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        presenter.bind(this)

        actionBar?.title = context.getString(R.string.app_name);
        actionBar?.setDisplayShowHomeEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onDestroyView() {
        presenter.unbind()
        super.onDestroyView()
    }

    override fun showArtists(artists: List<Artist>) = uiThread {
        adapter.setArtists(artists)
    }

    override fun updateArtist(artist: Artist) = uiThread {
        adapter.updateArtist(artist)
    }

}
