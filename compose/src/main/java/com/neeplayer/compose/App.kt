package com.neeplayer.compose

import androidx.compose.foundation.Text
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(container: AppStateContainer) {
    NeeTheme {
        val state = container.state.value

        Column {

            TopAppBar(title = { Text(text = "Neé", color = contentColor()) })


            Box {

                Box(modifier = Modifier.padding(bottom = 72.dp)) {
                    when (val screen = state.currentScreen) {
                        is Screen.Artists -> ArtistsScreen(artists = state.artists, container = container)
                        is Screen.Albums -> AlbumsScreen(
                            artist = screen.artist,
                            albums = state.albums.getValue(screen.artist.id).take(1),
                            nowPlayingSongId = state.nowPlaying?.song?.id,
                            container = container,
                        )
                    }
                }


                NowPlayingScreen(state = state.nowPlaying, container = container)
            }
        }
    }
}