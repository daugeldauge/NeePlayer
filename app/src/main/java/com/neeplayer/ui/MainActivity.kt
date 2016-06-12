package com.neeplayer.ui

import android.Manifest
import android.app.FragmentTransaction
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import com.neeplayer.*

import com.neeplayer.model.Preferences.Item.BooleanItem.SCROBBLING
import com.neeplayer.model.Preferences.Item.StringItem.SESSION_KEY
import com.neeplayer.ui.artists.ArtistsFragment
import com.neeplayer.model.Artist
import com.neeplayer.model.LastFmService
import com.neeplayer.model.NowPlayingModel
import com.neeplayer.model.Preferences
import com.neeplayer.ui.albums.AlbumsFragmentBuilder
import com.neeplayer.ui.now_playing.MusicService
import com.neeplayer.ui.now_playing.NowPlayingFragment

import org.jetbrains.anko.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

// TODO: refactor to MVP
class MainActivity : AppCompatActivity() {

    companion object { init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }}


    private var token: String? = null

    @Inject
    lateinit internal var lastFm: LastFmService

    @Inject
    lateinit internal var preferences: Preferences

    @Inject
    lateinit internal var nowPlayingModel: NowPlayingModel;

    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 42;

    private val nowPlayingFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.now_playing_fragment) as NowPlayingFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NeePlayerApp.component.inject(this)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            onReadStoragePermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onReadStoragePermissionGranted()
            } else {
                finish()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun onReadStoragePermissionGranted() {
        nowPlayingModel.tryRestoreNowPlaying()
        if (supportFragmentManager.findFragmentByTag(ArtistsFragment.TAG) == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, ArtistsFragment(), ArtistsFragment.TAG).commitAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, MusicService::class.java))
    }

    override fun onResume() {
        super.onResume()
        lastFm.getSession(token ?: return)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    preferences.put(SESSION_KEY, it.getJSONObject("session").getString("key"))
                    invalidateOptionsMenu()
                    toast(R.string.last_fm_auth_success)
                    token = null
                }, {
                    toast(R.string.last_fm_auth_error)
                })
    }

    fun navigateToArtistFragment(artist: Artist) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, AlbumsFragmentBuilder(artist).build())
                .addToBackStack(null)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.scrobbling).isChecked = preferences.getOrDefault(SCROBBLING)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isSignedIn = preferences.get(SESSION_KEY) != null

        menu.findItem(R.id.sign_in).isVisible = !isSignedIn
        menu.findItem(R.id.scrobbling).isVisible = isSignedIn
        menu.findItem(R.id.sign_out).isVisible = isSignedIn
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_in -> {
                lastFm.getToken().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            token = it.getString("token")

                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("http://www.last.fm/api/auth/?api_key=${LastFmService.apiKey}&token=$token")
                            startActivity(intent)
                        }, {
                            toast(R.string.last_fm_auth_error)
                        })
            }
            R.id.sign_out -> {
                preferences.remove(SESSION_KEY)
                invalidateOptionsMenu()
            }
            R.id.scrobbling -> {
                item.isChecked = !item.isChecked
                preferences.put(SCROBBLING, item.isChecked)
            }
            else -> return false
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed();
        return true;
    }

    override fun onBackPressed() {
        if (!nowPlayingFragment.tryCollapse()) {
            super.onBackPressed()
        }
    }
}
