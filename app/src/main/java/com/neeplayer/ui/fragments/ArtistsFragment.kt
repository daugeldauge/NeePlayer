package com.neeplayer.ui.fragments

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
import com.neeplayer.ui.actionBar
import com.neeplayer.ui.activities.MainActivity
import com.neeplayer.ui.adapters.ArtistAdapter
import com.neeplayer.ui.presenters.ArtistsPresenter
import com.neeplayer.ui.uiThread
import com.neeplayer.ui.views.ArtistsView
import javax.inject.Inject

class ArtistsFragment : Fragment(), ArtistsView {
    companion object {
        val TAG = ArtistsFragment::class.java.name;
    }

    @Inject
    lateinit internal var presenter: ArtistsPresenter

    private val adapter by lazy {
        ArtistAdapter(activity) {
            (activity as MainActivity).navigateToArtistFragment(it)
            presenter.onArtistClicked(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NeePlayerApp.component.inject(this)
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
