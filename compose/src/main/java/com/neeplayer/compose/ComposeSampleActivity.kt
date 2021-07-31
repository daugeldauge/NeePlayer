package com.neeplayer.compose

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import coil.Coil
import coil.ImageLoader
import coil.util.DebugLogger
import com.neeplayer.compose.db.Database
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ComposeSampleActivity : ComponentActivity() {

    private val container = AppStateContainer()

    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(container)
        }

        Coil.setImageLoader {
            ImageLoader.Builder(this)
                .componentRegistry { add(ThumbnailFetcher(this@ComposeSampleActivity)) }
                .logger(DebugLogger())
                .build()
        }

        val database = Database(contentResolver)

        mainScope.launch {
            container.state
                .map { it.currentScreen }
                .distinctUntilChanged()
                .collect { screen ->
                    when (screen) {
                        is Screen.Artists -> {
                            container.updateArtists(database.artists())
                        }
                        is Screen.Albums -> {
                            database.albums(screen.artist)
                                .map { AlbumWithSongs(it, database.songs(it)) }
                                .let(container::updateAlbums)
                        }
                    }
                }
        }

        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST_CODE) // TODO handle result gracefully
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    override fun onBackPressed() {
        if (!container.goBack()) {
            super.onBackPressed()
        }
    }

    companion object {
        init {
            val handler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                Log.e("FATAL CRASH", exception.message.orEmpty(), exception)
                handler?.uncaughtException(thread, exception)
            }
        }
    }
}

private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 42
