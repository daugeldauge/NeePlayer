package com.neeplayer.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

@Composable
fun App(container: AppStateContainer) {
    NeeTheme {
        val state = container.state.collectAsState().value

        Column {

            TopAppBar(title = { Text(text = "Neé", color = MaterialTheme.colors.onPrimary) })

            NowPlayingBottomSheet(state = state.nowPlaying, container = container) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (val screen = state.currentScreen) {
                        is Screen.Artists -> ArtistsScreen(artists = screen.artists, container = container)
                        is Screen.Albums -> AlbumsScreen(
                            artist = screen.artist,
                            albums = screen.albums,
                            nowPlayingSongId = state.nowPlaying?.song?.id,
                            container = container,
                        )
                    }
                }
            }
        }
    }
}
