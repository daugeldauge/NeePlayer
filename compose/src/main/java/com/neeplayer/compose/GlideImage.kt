package com.neeplayer.compose

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.Composable
import androidx.compose.onCommit
import androidx.compose.state
import androidx.ui.animation.Crossfade
import androidx.ui.core.Constraints
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.foundation.Image
import androidx.ui.graphics.Color
import androidx.ui.graphics.asImageAsset
import androidx.ui.graphics.painter.ColorPainter
import androidx.ui.graphics.painter.ImagePainter
import androidx.ui.graphics.painter.Painter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.Transition

@Composable
fun GlideImage(model: Any?, modifier: Modifier = Modifier) {
    val placeholderPainter = ColorPainter(Color.Transparent)
    val errorPainter = ColorPainter(NeeColors.imageAlt)
    val painter = state<Painter> { placeholderPainter }
    val context = ContextAmbient.current

    WithConstraints(modifier) {
        onCommit(model) {
            val glide = Glide.with(context)
            val target = glide
                .asBitmap()
                .load(model)
                .centerCrop()
                .override(constraints.maxWidth.glideSize, constraints.maxHeight.glideSize)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        painter.value = placeholderPainter
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        painter.value = placeholderPainter
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        painter.value = ImagePainter(resource.asImageAsset())
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        painter.value = errorPainter
                    }
                })


            onDispose {
                painter.value = placeholderPainter
                glide.clear(target)
            }
        }
        Crossfade(current = painter.value) { Image(it, modifier = modifier) }
    }
}

private val Int.glideSize
    get() = takeIf { it != Constraints.Infinity } ?: SIZE_ORIGINAL