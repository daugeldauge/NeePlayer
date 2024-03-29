package com.neeplayer.compose

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.size.Scale

@Composable
fun NowPlayingBottomSheetContent(
    state: NowPlayingState?,
    sheetValue: BottomSheetValue,
    actions: NowPlayingActions,
) {
    Box {
        Body(state = state, actions = actions)

        Crossfade(targetState = sheetValue) { value ->
            if (value == BottomSheetValue.Collapsed) {
                Header(state = state, onPlayPauseClick = { actions.playOrPause() })
            }
        }
    }
}

@Composable
private fun Body(state: NowPlayingState?, actions: NowPlayingActions) {
    Column {

        Image(
            painter = rememberImagePainter(data = state?.album?.art) {
                scale(Scale.FIT)
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                style = MaterialTheme.typography.body1.copy(fontSize = 22.sp),
                text = state?.song?.title.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                style = MaterialTheme.typography.body2.copy(fontSize = 18.sp),
                text = "${state?.artist?.name} — ${state?.album?.title}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {

            MusicControl(R.drawable.ic_fast_rewind_black_48dp) { actions.playPrevious() }

            MusicControl(state.playPauseResource()) { actions.playOrPause() }

            MusicControl(R.drawable.ic_fast_forward_black_48dp) { actions.playNext() }
        }

        Box(modifier = Modifier.padding(8.dp)) {

            Slider(
                value = state?.progress?.toFloat() ?: 0f,
                valueRange = 0f..(state?.song?.duration?.toFloat() ?: 1f),
                onValueChange = { value -> actions.seekTo(value.toLong()) },
            )

            Row(modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 40.dp)
                .align(Alignment.BottomCenter)) {

                Text(
                    style = MaterialTheme.typography.caption,
                    text = state?.progress.formatDuration(),
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    style = MaterialTheme.typography.caption,
                    text = state?.song?.duration.formatDuration(),
                )

            }
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
private fun Header(state: NowPlayingState?, onPlayPauseClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .height(72.dp)
            .background(MaterialTheme.colors.background)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Image(
            painter = rememberImagePainter(data = state?.album?.art) {
                scale(Scale.FIT)
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )

        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 12.dp, end = 12.dp)) {

            Text(
                text = state?.song?.title.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body1
            )

            Text(
                text = state?.artist?.name.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2
            )

        }

        MusicControl(iconResource = state.playPauseResource(),
            width = 48.dp,
            onClick = onPlayPauseClick)
    }

}

@Composable
private fun MusicControl(
    @DrawableRes iconResource: Int,
    width: Dp = 88.dp,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .width(width)
            .clickable(
                onClick = onClick,
                indication = rememberRipple(bounded = false, radius = 56.dp),
                interactionSource = remember { MutableInteractionSource() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(id = iconResource), contentDescription = null)
    }
}

private fun NowPlayingState?.playPauseResource() =
    if (this?.playing != true) R.drawable.ic_play_arrow_black_48dp else R.drawable.ic_pause_black_48dp

@Preview
@Composable
fun PreviewNowPlayingScreen() = NeeTheme {
    Body(NowPlayingState(
        playlist = listOf(PlaylistItem(
            song = Sample.songs.first(),
            album = Sample.albums.first().album,
            artist = Sample.artists.first(),
        )),
        position = 0,
        playing = true,
        progress = 0,
    ), AppStateContainer())
}
