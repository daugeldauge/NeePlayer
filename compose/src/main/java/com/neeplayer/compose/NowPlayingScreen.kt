package com.neeplayer.compose

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade

class NowPlayingState(val song: Song, val album: Album, val artist: Artist, val playing: Boolean, val progress: Long)

@Composable
fun NowPlayingScreen(state: NowPlayingState?) {
    val drawerState = rememberBottomDrawerState(initialValue = BottomDrawerValue.Expanded)

    BottomDrawerLayout(
        drawerState = drawerState,
        drawerContent = { DrawerContent(state = state, drawerValue = drawerState.targetValue) },
        closedAnchorOffset = headerHeight
    ) {
        Image(painter = ColorPainter(Color.Cyan), modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun DrawerContent(state: NowPlayingState?, drawerValue: BottomDrawerValue) {
    Stack {
        Body(state = state)

        Crossfade(current = drawerValue) { value ->
            if (value == BottomDrawerValue.Closed) {
                Header(state = state)
            }
        }
    }
}

@Composable
private fun Body(state: NowPlayingState?) {
    Column {

        WithConstraints(Modifier.fillMaxWidth()) {
            CoilImageWithCrossfade(
                modifier = Modifier.size(maxWidth),
                data = state?.album?.art.orEmpty(),
                contentScale = ContentScale.Crop,
                getFailurePainter = { ColorPainter(Color.LightGray) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            MusicControl(R.drawable.ic_fast_rewind_black_48dp)

            MusicControl(state.playPauseResourse())

            MusicControl(R.drawable.ic_fast_forward_black_48dp)
        }

        Stack(modifier = Modifier.padding(8.dp)) {

            val progress = if (state != null) state.progress.toFloat() / state.song.duration else 0f

            Slider(value = progress, onValueChange = {})

            Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp).align(Alignment.BottomCenter)) {

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
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
private fun MusicControl(@DrawableRes iconResource: Int, onClick: () -> Unit = {}) {
    IconButton(modifier = Modifier.width(88.dp), onClick = onClick) {
        Icon(asset = vectorResource(id = iconResource))
    }
}

@Composable
private fun Header(state: NowPlayingState?, onPlayPauseClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.height(headerHeight)
            .background(MaterialTheme.colors.background)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        CoilImageWithCrossfade(
            modifier = Modifier.size(64.dp),
            data = state?.album?.art.orEmpty(),
            contentScale = ContentScale.Crop,
            getFailurePainter = { ColorPainter(Color.LightGray) },
        )

        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 12.dp)) {

            Text(
                text = state?.artist?.name.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body1
            )

            Text(
                text = state?.song?.title.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2
            )

        }

        IconButton(onClick = onPlayPauseClick) {
            Icon(asset = vectorResource(state.playPauseResourse()))
        }

    }

}

private fun NowPlayingState?.playPauseResourse() = if (this?.playing != true) R.drawable.ic_play_arrow_black_48dp else R.drawable.ic_pause_black_48dp

private val headerHeight = 72.dp

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

