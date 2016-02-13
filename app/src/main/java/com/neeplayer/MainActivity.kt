package com.neeplayer

import android.app.Activity
import android.os.Bundle
import java.util.*

class MainActivity : Activity() {

    val nowPlayingFragment by lazy {
        fragmentManager.findFragmentById(R.id.now_playing_fragment) as NowPlayingFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentManager.beginTransaction().add(R.id.fragment_container, MainFragment()).commit()

    }

    fun navigateToArtistFragment(artist: Artist) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ArtistFragmentBuilder(artist).build())
                .addToBackStack(null)
                .commit()
    }

    fun navigateToNowPlayingFragment(artistName: String, albumList: ArrayList<Album>, nowPlaying: Index) {
        nowPlayingFragment.update(artistName, albumList, nowPlaying)
    }

    override fun onBackPressed() {
        if (nowPlayingFragment.expanded) {
            nowPlayingFragment.expanded = false
        } else {
            super.onBackPressed()
        }
    }
}
