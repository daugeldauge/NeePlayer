package com.neeplayer.compose

import android.graphics.Bitmap
import androidx.compose.Composable
import androidx.compose.onCommit
import androidx.compose.state
import androidx.ui.animation.Crossfade
import androidx.ui.core.ContextAmbient
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.asImageAsset
import androidx.ui.layout.size
import androidx.ui.res.imageResource
import androidx.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

@Composable
fun GlideImage(model: Any?) {
    val result = state<ImageAsset?> { null }
    val context = ContextAmbient.current
    val density = DensityAmbient.current

    onCommit(model) {
        val listener = object : RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                result.value = null
                return false
            }

            override fun onResourceReady(resource: Bitmap, model: Any?, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                result.value = resource.asImageAsset()
                return true
            }
        }

        val glide = Glide.with(context)
        val target = with(density) {
            glide
                    .asBitmap()
                    .load(model)
                    .centerCrop()
                    .addListener(listener)
                    .preload(90.dp.toIntPx().value, 90.dp.toIntPx().value)
        }

        onDispose {
            glide.clear(target)
        }
    }

    Crossfade(current = result.value) { asset ->
        Image(asset ?: imageResource(id = android.R.drawable.star_on), modifier = Modifier.size(90.dp))
    }
}