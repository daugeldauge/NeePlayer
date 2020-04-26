package com.neeplayer.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.neeplayer.compose.setSampleContent

class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSampleContent()
    }
}