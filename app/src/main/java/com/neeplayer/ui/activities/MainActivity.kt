package com.neeplayer.ui.activities

import android.app.FragmentTransaction
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.neeplayer.ui.fragments.MainFragment
import com.neeplayer.ui.fragments.NowPlayingFragment
import com.neeplayer.R
import com.neeplayer.model.*
import com.neeplayer.ui.fragments.ArtistFragmentBuilder

class MainActivity : AppCompatActivity() {

    private val nowPlayingFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.now_playing_fragment) as NowPlayingFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, MainFragment()).commit()
        }

        Model.init(this)
    }

    fun navigateToArtistFragment(artist: Artist) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, ArtistFragmentBuilder(artist).build())
                .addToBackStack(null)
                .commit()
    }

    fun navigateToNowPlayingFragment(artist: Artist, albumList: List<Album>, nowPlaying: Index.Song) {
        Model.nowPlaying = Playlist(artist, albumList, nowPlaying)
        nowPlayingFragment.play()
    }

    override fun onBackPressed() {
        if (!nowPlayingFragment.tryCollapse()) {
            super.onBackPressed()
        }
    }
}
