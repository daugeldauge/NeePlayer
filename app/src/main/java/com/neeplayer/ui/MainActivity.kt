package com.neeplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.neeplayer.R
import com.neeplayer.di.ActivityModule
import com.neeplayer.di.component
import com.neeplayer.model.NowPlayingService
import com.neeplayer.toast
import com.neeplayer.ui.auth.AuthPresenter
import com.neeplayer.ui.auth.AuthView
import com.neeplayer.ui.now_playing.MusicService
import com.neeplayer.ui.now_playing.NowPlayingFragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AuthView {

    companion object {
        const val OPEN_NOW_PLAYING_ACTION = "open_now_playing"
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 42
    }

    val activityComponent by lazy { component.plus(ActivityModule(this)) }

    @Inject
    internal lateinit var presenter: AuthPresenter

    @Inject
    internal lateinit var nowPlayingModel: NowPlayingService

    @Inject
    internal lateinit var router: Router


    private var menuElements = emptySet<AuthView.MenuElement>()
    private var scrobblingToggleChecked = true

    private val scope = MainScope()
    private val resumesChannel = Channel<Unit>()
    private val optionsItemSelectionsChannel = Channel<Int>()

    private val nowPlayingFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.now_playing_fragment) as NowPlayingFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent.inject(this)

        setContentView(R.layout.activity_main)
        presenter.bind(scope, this)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST_CODE)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == OPEN_NOW_PLAYING_ACTION) {
            nowPlayingFragment.expand()
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

    private fun onReadStoragePermissionGranted() {
        nowPlayingModel.tryRestoreNowPlaying()
        if (!router.areArtistsShown()) {
            router.goToArtists()
        }
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, MusicService::class.java))
    }

    override fun onResume() {
        super.onResume()
        resumesChannel.offer(Unit)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.sign_in).isVisible = menuElements.contains(AuthView.MenuElement.SIGN_IN)
        menu.findItem(R.id.scrobbling).isVisible = menuElements.contains(AuthView.MenuElement.SCROBBLING)
        menu.findItem(R.id.sign_out).isVisible = menuElements.contains(AuthView.MenuElement.SIGN_OUT)
        menu.findItem(R.id.scrobbling).isChecked = scrobblingToggleChecked
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemSelectionsChannel.offer(item.itemId)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (!nowPlayingFragment.tryCollapse()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override val resumes get() = resumesChannel.consumeAsFlow()
    override val signInClicks get() = optionsItemSelectionsChannel.consumeAsFlow().filter { it == R.id.sign_in }
    override val signOutClicks get() = optionsItemSelectionsChannel.consumeAsFlow().filter { it == R.id.sign_out }
    override val scrobbleToggles get() = optionsItemSelectionsChannel.consumeAsFlow().filter { it == R.id.scrobbling }

    override fun showAuthSuccess() = toast(R.string.last_fm_auth_success)

    override fun showAuthError() = toast(R.string.last_fm_auth_error)

    override fun showMenuElements(items: Set<AuthView.MenuElement>) {
        menuElements = items
        invalidateOptionsMenu()
    }

    override fun setScrobblingChecked(checked: Boolean) {
        scrobblingToggleChecked = checked
        invalidateOptionsMenu()
    }

    override fun showAuthView(uri: Uri) = startActivity(Intent(Intent.ACTION_VIEW, uri))
    //endregion

}
