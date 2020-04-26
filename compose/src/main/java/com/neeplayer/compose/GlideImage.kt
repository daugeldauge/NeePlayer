package com.neeplayer.compose

import android.graphics.Bitmap
import androidx.compose.Composable
import androidx.compose.onPreCommit
import androidx.compose.state
import androidx.ui.core.ContextAmbient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

@Composable
fun glideBitmap(model: Any?): Bitmap? {
    val result = state<Bitmap?> { null }
    val context = ContextAmbient.current
    onPreCommit(model) {
        val listener = object : RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                result.value = null
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                result.value = resource
                return true
            }
        }

        Glide.with(context)
                .asBitmap()
                .load(model)
                .addListener(listener)
                .preload()

    }
    return result.value
}