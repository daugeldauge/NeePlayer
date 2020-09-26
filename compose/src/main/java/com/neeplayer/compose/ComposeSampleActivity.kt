package com.neeplayer.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.setContent

class ComposeSampleActivity : ComponentActivity() {

    private val container = AppStateContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container.state.value = AppState(
            artists = Sample.artists,
            albums = mapOf(
                0L to Sample.albums,
                1L to Sample.albums,
                2L to Sample.albums,
                3L to Sample.albums,
            ),
        )

        setContent {
            App(container)
        }
    }

    override fun onBackPressed() {
        if (!container.goBack()) {
            super.onBackPressed()
        }
    }
}