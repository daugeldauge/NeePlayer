package com.neeplayer.compose

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.ImagePainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.Constraints
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.Transition

@Composable
fun GlideImage(model: Any?, modifier: Modifier = Modifier) {
    val placeholderPainter = ColorPainter(Color.Transparent)
    val errorPainter = ColorPainter(NeeColors.imageAlt)
    val painter = remember { mutableStateOf<Painter>(placeholderPainter) }
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