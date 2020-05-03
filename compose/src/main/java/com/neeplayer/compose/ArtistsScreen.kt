package com.neeplayer.compose

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.padding
import androidx.ui.layout.size
import androidx.ui.material.MaterialTheme
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp

@Composable
fun ArtistsScreen(artists: List<Artist>) {
    AdapterList(data = artists) {
        ArtistItem(artist = it)
    }
}

@Composable
fun ArtistItem(artist: Artist) {
    Row(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp, top = 5.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        GlideImage(artist.imageUrl, modifier = Modifier.size(90.dp))
        Column(modifier = Modifier.padding(all = 10.dp)) {
            Text(text = artist.name, style = MaterialTheme.typography.body1)
            Text(text = "${artist.numberOfAlbums} albums, ${artist.numberOfSongs} songs", style = MaterialTheme.typography.body2)
        }
    }
}

@Preview
@Composable
fun PreviewArtistsScreen() = NeeTheme {
    ArtistsScreen(artists = Sample.artists)
}

