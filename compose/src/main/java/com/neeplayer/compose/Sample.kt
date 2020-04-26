package com.neeplayer.compose

import androidx.activity.ComponentActivity
import androidx.ui.core.setContent

object Sample {
    val artists = listOf(
            Artist(0, "David Bowie", 10, 2),
            Artist(0, "Yes", 10, 2),
            Artist(0, "Grateful Dead", 10, 2),
            Artist(0, "Joy Division", 10, 2)
    )
}

fun ComponentActivity.setSampleContent() {
    setContent {
        PreviewArtistsScreen()
    }
}