package com.neeplayer.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun App(container: AppStateContainer) {
    NeeTheme {
        val state = container.state.collectAsState().value

        val scaffoldState = rememberBottomSheetScaffoldState()

        val sheetExpanded = scaffoldState.bottomSheetState.targetValue == BottomSheetValue.Expanded

        val scope = rememberCoroutineScope()

        Column {

            TopAppBar(
                title = {
                    Text(
                        text = when {
                            sheetExpanded -> "Now playing"
                            state.currentScreen is Screen.Albums -> state.currentScreen.artist.name
                            else -> "Neé"
                        },
                        color = MaterialTheme.colors.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.bottomSheetState.collapse() } }) {
                        Icon(
                            imageVector = if (sheetExpanded) Icons.Filled.Close else Icons.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    val showMenu = remember { mutableStateOf(false) }

                    IconButton(onClick = { showMenu.value = !showMenu.value }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false }
                    ) {
                        DropdownMenuItem(onClick = { /*TODO*/ }) {
                            Text(text = "Scrobbling")
                            Spacer(modifier = Modifier.weight(1f))
                            Checkbox(checked = true, onCheckedChange = null)
                        }
                        DropdownMenuItem(onClick = { /*TODO*/ }) {
                            Text(text = "Sign in to Last.fm")
                        }
                    }
                }
            )

            BottomSheetScaffold(
                sheetContent = {
                    NowPlayingBottomSheetContent(
                        state = state.nowPlaying,
                        sheetValue = scaffoldState.bottomSheetState.targetValue,
                        actions = container,
                    )
                },
                scaffoldState = scaffoldState,
                sheetPeekHeight = 72.dp,
            ) { padding ->

                Box(modifier = Modifier.padding(padding)) {
                    when (val screen = state.currentScreen) {
                        is Screen.Artists -> ArtistsScreen(artists = screen.artists, actions = container)
                        is Screen.Albums -> AlbumsScreen(
                            artist = screen.artist,
                            albums = screen.albums,
                            nowPlayingSongId = state.nowPlaying?.song?.id,
                            actions = container,
                        )
                    }
                }
            }
        }
    }
}
