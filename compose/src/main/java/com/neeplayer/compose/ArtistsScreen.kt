package com.neeplayer.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.size.Scale

@Composable
fun ArtistsScreen(artists: List<Artist>, actions: ArtistsActions) {
    LazyColumn {
        items(artists) {
            ArtistItem(artist = it, actions = actions)
        }
    }
}

@Composable
fun ArtistItem(artist: Artist, actions: ArtistsActions) {
    Row(
        modifier = Modifier
            .clickable(onClick = { actions.goToAlbums(artist) })
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp, top = 5.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(data = artist.imageUrl.orEmpty()) {
                scale(Scale.FILL)
            },
            contentDescription = null,
            modifier = Modifier.size(90.dp),
        )
        Column(modifier = Modifier.padding(all = 10.dp)) {
            Text(text = artist.name, style = MaterialTheme.typography.body1)
            Text(text = "${artist.numberOfAlbums} albums, ${artist.numberOfSongs} songs", style = MaterialTheme.typography.body2)
        }
    }
}

@Preview
@Composable
fun PreviewArtistsScreen() = NeeTheme {
    ArtistsScreen(artists = Sample.artists, AppStateContainer())
}

