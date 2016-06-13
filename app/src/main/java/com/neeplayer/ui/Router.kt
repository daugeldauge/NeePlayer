package com.neeplayer.ui

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.neeplayer.R
import com.neeplayer.model.Artist
import com.neeplayer.ui.albums.AlbumsFragmentBuilder
import com.neeplayer.ui.artists.ArtistsFragment

class Router(activity: AppCompatActivity) {

    val fragmentManager = activity.supportFragmentManager

    fun goToArtists() = showFragment(ArtistsFragment(), false, ArtistsFragment.TAG)

    fun areArtistsShown() = (fragmentManager.findFragmentByTag(ArtistsFragment.TAG) != null)

    fun gotToAlbums(artist: Artist) = showFragment(AlbumsFragmentBuilder(artist).build())

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean = true, tag: String? = null) {
        fragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.fragment_container, fragment, tag)
            .apply {
                if (addToBackStack) {
                    addToBackStack(null)
                }
            }
            .commit()
    }

}