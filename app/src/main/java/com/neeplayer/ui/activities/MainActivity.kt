package com.neeplayer.ui.activities

import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.neeplayer.R
import com.neeplayer.ui.fragments.MainFragment
import com.neeplayer.model.Artist
import com.neeplayer.ui.fragments.ArtistFragmentBuilder
import com.neeplayer.ui.views.impl.MusicService
import com.neeplayer.ui.views.impl.NowPlayingFragment

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
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, MusicService::class.java))
    }

    fun navigateToArtistFragment(artist: Artist) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, ArtistFragmentBuilder(artist).build())
                .addToBackStack(null)
                .commit()
    }

    override fun onBackPressed() {
        if (!nowPlayingFragment.tryCollapse()) {
            super.onBackPressed()
        }
    }
}
