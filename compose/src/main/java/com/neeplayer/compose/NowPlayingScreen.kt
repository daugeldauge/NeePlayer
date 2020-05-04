package com.neeplayer.compose

import androidx.annotation.DrawableRes
import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.drawShadow
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.graphics.painter.ColorPainter
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.res.vectorResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp

class NowPlayingState(val song: Song, val album: Album, val artist: Artist, val playing: Boolean, val progress: Long)


@Composable
fun NowPlayingScreen(state: NowPlayingState?) {
    BottomDrawerLayout(drawerState = DrawerState.Opened, onStateChange = {}, drawerContent = { DrawerContent(state = state) }) {
        Image(painter = ColorPainter(Color.Cyan))
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

        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = ColorPainter(Color.Cyan)
        )

        GlideImage(
            modifier = Modifier.fillMaxWidth(),
            model = state?.album?.art
        )

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

