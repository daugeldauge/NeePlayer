package com.neeplayer.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview

class NowPlayingState(val song: Song, val album: Album, val artist: Artist, val playing: Boolean, val progress: Long)


@Composable
fun NowPlayingScreen(state: NowPlayingState?) {
    BottomDrawerLayout(drawerState = rememberBottomDrawerState(initialValue = BottomDrawerValue.Open), drawerContent = { DrawerContent(state = state) }) {
        Image(painter = ColorPainter(Color.Cyan), modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun DrawerContent(state: NowPlayingState?) {
    Stack(modifier = Modifier.drawShadow(elevation = 12.dp)) {
        Body(state = state)
        Header(state = state)
    }
}

@Composable
private fun Body(state: NowPlayingState?) {
    Column {

        WithConstraints(Modifier.fillMaxWidth()) {
            GlideImage(
                modifier = Modifier.size(maxWidth),
                model = state?.album?.art
            )
        }

        Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)) {

            Text(
                style = MaterialTheme.typography.body2,
                text = state?.progress.formatDuration()
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                style = MaterialTheme.typography.body2,
                text = state?.song?.duration.formatDuration()
            )

        }

        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp),
                horizontalGravity = Alignment.CenterHorizontally
            ) {

                Text(
                    style = MaterialTheme.typography.body1.copy(fontSize = 22.sp),
                    text = state?.song?.title.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    style = MaterialTheme.typography.body2.copy(fontSize = 18.sp),
                    text = "${state?.artist?.name} — ${state?.album?.title}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalGravity = Alignment.CenterVertically
            ) {

                MusicControl(R.drawable.ic_fast_rewind_black_48dp)

                MusicControl(if (state?.playing != true) R.drawable.ic_play_arrow_black_48dp else R.drawable.ic_pause_black_48dp)

                MusicControl(R.drawable.ic_fast_forward_black_48dp)
            }

        }

    }
}

@Composable
private fun MusicControl(@DrawableRes iconResource: Int, onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(modifier = Modifier.width(88.dp), asset = vectorResource(id = iconResource))
    }
}

@Composable
private fun Header(state: NowPlayingState?) {

}

@Preview
@Composable
fun PreviewNowPlayingScreen() = NeeTheme {
    NowPlayingScreen(NowPlayingState(
        song = Sample.songs.first(),
        album = Sample.albums.first().album,
        artist = Sample.artists.first(),
        playing = true,
        progress = 132_000
    ))
}

