package com.neeplayer.compose

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade

@Composable
fun ArtistsScreen(artists: List<Artist>) {
    LazyColumnFor(items = artists, itemContent = {
        ArtistItem(artist = it)
    })
}

@Composable
fun ArtistItem(artist: Artist) {
    Row(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp, top = 5.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoilImageWithCrossfade(
            data = artist.imageUrl.orEmpty(),
            modifier = Modifier.size(90.dp),
            contentScale = ContentScale.Crop,
            getFailurePainter = { ColorPainter(Color.LightGray) },
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
    ArtistsScreen(artists = Sample.artists)
}

