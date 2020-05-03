package com.neeplayer.compose

import androidx.compose.Composable
import androidx.ui.foundation.AdapterList
import androidx.ui.tooling.preview.Preview

@Composable
fun AlbumsScreen(albums: List<AlbumWithSongs>) {
    val compositions = albums.asSequence().flatMap { sequenceOf(it.album) + it.songs }.toList()
    
    AdapterList(data = compositions) { composition ->
        when (composition) {
            is Album -> AlbumSummaryView(album = composition)
            is Song -> SongView(song = composition)
        }
    }
}

@Composable
fun SongView(song: Song) {

}

@Composable
fun AlbumSummaryView(album: Album) {

}

@Preview
@Composable
fun PreviewAlbumsScreen() = NeeTheme {
    AlbumsScreen(albums = Sample.albums)
}

