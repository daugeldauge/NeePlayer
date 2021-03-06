package com.neeplayer.compose

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import java.util.concurrent.TimeUnit

@Composable
fun AlbumsScreen(artist: Artist, albums: List<AlbumWithSongs>, nowPlayingSongId: Long?, container: AppStateContainer) {
    val compositions = albums.asSequence().flatMap { sequenceOf(it) + it.songs }.toList()

    LazyColumnFor(items = compositions, itemContent = { composition ->
        when (composition) {
            is AlbumWithSongs -> AlbumSummaryView(albumWithSongs = composition)
            is Song -> SongView(song = composition, playing = composition.id == nowPlayingSongId, onClick = {
                container.playSong(song = composition, artist = artist, albums = albums)
            })
        }
    })
}

@Composable
fun SongView(song: Song, playing: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick)
            .padding(start = 20.dp, end = 25.dp, top = 10.dp, bottom = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(35.dp).height(24.dp)) {
            if (playing) {
                Icon(vectorResource(id = R.drawable.ic_equalizer_black_24dp))
            } else {
                Text(
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.width(30.dp).align(Alignment.CenterEnd),
                    text = song.formattedTrack
                )
            }
        }

        Text(
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f),
            text = song.title.orEmpty(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(start = 4.dp),
            text = song.duration.formatDuration()
        )
    }
}

@Composable
fun AlbumSummaryView(albumWithSongs: AlbumWithSongs) {
    val album = albumWithSongs.album

    Row(modifier = Modifier.height(130.dp).padding(all = 15.dp)) {
        CoilImageWithCrossfade(
            modifier = Modifier.size(100.dp),
            data = album.art.orEmpty(),
            contentScale = ContentScale.Crop,
            getFailurePainter = { ColorPainter(Color.LightGray) },
        )

        Column(modifier = Modifier.padding(10.dp)) {

            Text(
                style = MaterialTheme.typography.body1,
                text = album.title.orEmpty(),
                maxLines = 2
            )

            Text(
                style = MaterialTheme.typography.body2,
                text = albumWithSongs.description
            )

            Text(
                style = MaterialTheme.typography.body2,
                text = album.formattedYear
            )

        }
    }
}

private val Song.formattedTrack: String
    get() = track?.rem(1000)?.toString().orEmpty()

private val AlbumWithSongs.description: String
    get() = "%d songs, %d min".format(songs.size, TimeUnit.MILLISECONDS.toMinutes(songs.map { it.duration }.sum()))

private val Album.formattedYear: String
    get() = year?.takeIf { it > 0 }?.toString().orEmpty()


@Preview
@Composable
fun PreviewAlbumsScreen() = NeeTheme {
    AlbumsScreen(
        artist = Sample.artists.first(),
        albums = Sample.albums,
        nowPlayingSongId = 3,
        container = AppStateContainer()
    )
}

