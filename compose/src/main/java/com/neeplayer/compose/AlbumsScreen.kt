package com.neeplayer.compose

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.res.vectorResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import java.util.concurrent.TimeUnit

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
    Row(modifier = Modifier.padding(start = 20.dp, end = 25.dp, top = 10.dp, bottom = 10.dp), verticalGravity = Alignment.CenterVertically) {
        Stack(modifier = Modifier.width(35.dp)) {
            if (song.nowPlaying) {
                Icon(vectorResource(id = R.drawable.ic_equalizer_black_24dp))
            } else {
                Text(
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.width(30.dp).gravity(Alignment.CenterEnd),
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
fun AlbumSummaryView(album: Album) {
    Row(modifier = Modifier.height(130.dp).padding(all = 15.dp)) {
        GlideImage(
            modifier = Modifier.size(100.dp),
            model = album.art
        )

        Column(modifier = Modifier.padding(10.dp)) {

            Text(
                style = MaterialTheme.typography.body1,
                text = album.title.orEmpty(),
                maxLines = 2
            )

            Text(
                style = MaterialTheme.typography.body2,
                text = album.description
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

private val Album.description: String
    get() = "%d songs, %d min".format(numberOfSongs, TimeUnit.MILLISECONDS.toMinutes(duration))

private val Album.formattedYear: String
    get() = year?.takeIf { it > 0 }?.toString().orEmpty()


@Preview
@Composable
fun PreviewAlbumsScreen() = NeeTheme {
    AlbumsScreen(albums = Sample.albums)
}

