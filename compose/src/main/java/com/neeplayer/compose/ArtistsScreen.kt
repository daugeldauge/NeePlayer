package com.neeplayer.compose

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.padding
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.lightColorPalette
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
    Row(modifier = Modifier.padding(
            start = 10.dp,
            end = 10.dp,
            bottom = 5.dp,
            top = 5.dp
    )) {
        GlideImage(model = artist.imageUrl)
        Column(modifier = Modifier.padding(all = 10.dp)) {
            Text(text = artist.name, style = MaterialTheme.typography.body1)
            Text(text = "${artist.numberOfAlbums} albums, ${artist.numberOfSongs} songs", style = MaterialTheme.typography.body2)
        }
    }
}

@Preview
@Composable
fun PreviewArtistsScreen() {
    MaterialTheme(colors = lightColorPalette()) {
        Surface {
            ArtistsScreen(artists = listOf(
                    Artist(0, "David Bowie", 10, 2),
                    Artist(0, "Yes", 10, 2),
                    Artist(0, "Grateful Dead", 10, 2),
                    Artist(0, "Joy Division", 10, 2)
            ))
        }
    }
}

