package com.neeplayer.compose

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.graphics.painter.ColorPainter
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.padding
import androidx.ui.layout.size
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.lightColorPalette
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Size
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
        GlideImage(artist.imageUrl, modifier = Modifier.size(90.dp))
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
            ArtistsScreen(artists = Sample.artists)
        }
    }
}

