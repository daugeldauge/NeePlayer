package com.neeplayer.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp

class ComposeSampleActivity : ComponentActivity() {

    enum class Screen {
        Home, Artists, Albums, NowPlaying
    }

    private val state = mutableStateOf(Screen.Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            when (state.value) {
                Screen.Home -> HomeScreen()
                Screen.Artists -> PreviewArtistsScreen()
                Screen.Albums -> PreviewAlbumsScreen()
                Screen.NowPlaying -> PreviewNowPlayingScreen()
            }
        }
    }

    @Composable
    private fun HomeScreen() = NeeTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Screen.Artists.button()
            Screen.Albums.button()
            Screen.NowPlaying.button()
        }
    }

    @Composable
    private fun Screen.button() {
        Button(modifier = Modifier.padding(10.dp), onClick = { state.value = this }) {
            Text(text = name, color = Color.White)
        }
    }

    override fun onBackPressed() {
        if (state.value != Screen.Home) {
            state.value = Screen.Home
        } else {
            super.onBackPressed()
        }
    }
}