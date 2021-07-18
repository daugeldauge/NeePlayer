package com.neeplayer.ui.common

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.util.concurrent.TimeUnit

@BindingAdapter("imageUri")
fun loadImage(view: ImageView, uri: String?) = Glide.with(view.context).run {
    if (uri != null) {
        load(uri).into(view)
    } else {
        clear(view)
    }
}

@BindingAdapter("drawable")
fun setDrawable(view: ImageView, drawable: Drawable) = view.setImageDrawable(drawable)

@BindingAdapter("duration")
fun setFormattedDuration(view: TextView, duration: Int) {
    val min = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
    val sec = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(min)
    view.text = "%d:%02d".format(min, sec)
}
