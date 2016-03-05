package com.neeplayer

import android.app.FragmentTransaction
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    val nowPlayingFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.now_playing_fragment) as NowPlayingFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, MainFragment()).commit()
        }
    }

    fun navigateToArtistFragment(artist: Artist) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, ArtistFragmentBuilder(artist).build())
                .addToBackStack(null)
                .commit()
    }

    fun navigateToNowPlayingFragment(artistName: String, albumList: ArrayList<Album>, nowPlaying: Index) {
        nowPlayingFragment.update(artistName, albumList, nowPlaying)
    }

    override fun onBackPressed() {
        if (!nowPlayingFragment.tryCollapse()) {
            super.onBackPressed()
        }
    }
}
