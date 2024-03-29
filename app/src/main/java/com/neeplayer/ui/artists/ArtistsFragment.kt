package com.neeplayer.ui.artists

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neeplayer.R
import com.neeplayer.model.Artist
import com.neeplayer.ui.CoroFragment
import com.neeplayer.ui.MainActivity
import com.neeplayer.ui.common.actionBar
import com.neeplayer.ui.common.uiThread

class ArtistsFragment : CoroFragment(R.layout.fragment_artists), ArtistsView {
    companion object {
        val TAG: String = ArtistsFragment::class.java.name
    }

    private val presenter by lazy {
        (requireActivity() as MainActivity).koinScope.get<ArtistsPresenter>()
    }

    private val adapter by lazy {
        ArtistAdapter(requireContext()) {
            presenter.onArtistClicked(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view as RecyclerView

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        presenter.bind(viewScope, this)

        actionBar?.title = view.context.getString(R.string.app_name)
        actionBar?.setDisplayShowHomeEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun showArtists(artists: List<Artist>) = uiThread {
        adapter.setArtists(artists)
    }

    override fun updateArtist(artist: Artist) = uiThread {
        adapter.updateArtist(artist)
    }

}
