package com.neeplayer.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.setContent

class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreviewArtistsScreen()
        }
    }
}