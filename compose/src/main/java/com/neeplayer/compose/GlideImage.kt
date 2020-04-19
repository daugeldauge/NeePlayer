package com.neeplayer.compose

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.Composable
import androidx.compose.onCommit
import androidx.compose.state
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.foundation.Canvas
import androidx.ui.foundation.Image
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.asImageAsset
import androidx.ui.layout.fillMaxSize
import androidx.ui.unit.IntPx
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.Transition

/**
 *  Copied from https://github.com/mvarnagiris/compose-glide-image
 */
@Composable
fun GlideImage(
        model: Any?,
        customize: RequestBuilder<Bitmap>.() -> RequestBuilder<Bitmap> = { this }
) {
    WithConstraints { constraints, _ ->
        val image = state<ImageAsset?> { null }
        val drawable = state<Drawable?> { null }
        val context = ContextAmbient.current

        onCommit(model) {
            val target = object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    image.value = null
                    drawable.value = placeholder
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    image.value = resource.asImageAsset()
                }
            }

            val width = if (constraints.maxWidth > IntPx.Zero && constraints.maxWidth < IntPx.Infinity) {
                constraints.maxWidth.value
            } else {
                SIZE_ORIGINAL
            }

            val height = if (constraints.maxHeight > IntPx.Zero && constraints.maxHeight < IntPx.Infinity) {
                constraints.maxHeight.value
            } else {
                SIZE_ORIGINAL
            }

            val glide = Glide.with(context)
            glide
                    .asBitmap()
                    .load(model)
                    .override(width, height)
                    .let(customize)
                    .into(target)

            onDispose {
                image.value = null
                drawable.value = null
                glide.clear(target)
            }
        }

        val theImage = image.value
        val theDrawable = drawable.value
        if (theImage != null) {
            Image(asset = theImage)
        } else if (theDrawable != null) {
            Canvas(modifier = Modifier.fillMaxSize()) { theDrawable.draw(nativeCanvas) }
        }
    }
}