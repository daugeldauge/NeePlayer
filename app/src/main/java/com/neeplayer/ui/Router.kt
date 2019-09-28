package com.neeplayer.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.neeplayer.R
import com.neeplayer.model.Artist
import com.neeplayer.ui.albums.AlbumsFragment
import com.neeplayer.ui.artists.ArtistsFragment

class Router(activity: AppCompatActivity) {

    private val fragmentManager = activity.supportFragmentManager

    fun goToArtists() = showFragment(ArtistsFragment(), false, ArtistsFragment.TAG)

    fun areArtistsShown() = (fragmentManager.findFragmentByTag(ArtistsFragment.TAG) != null)

    fun gotToAlbums(artist: Artist) = showFragment(AlbumsFragment(artist))

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean = true, tag: String? = null) {
        fragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.fragment_container, fragment, tag)
            .apply {
                if (addToBackStack) {
                    addToBackStack(null)
                }
            }
            .commit()
    }

}