package com.neeplayer.ui.common

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.util.concurrent.TimeUnit

@BindingAdapter("imagePath")
fun loadImageFile(view: ImageView, path: String?) = loadImage(view, "file://" + path)

@BindingAdapter("imageURL")
fun loadImage(view: ImageView, url: String?) = Glide.with(view.context).load(url).into(view)

@BindingAdapter("drawable")
fun setDrawable(view: ImageView, drawable: Drawable) = view.setImageDrawable(drawable)

@BindingAdapter("duration")
fun setFormattedDuration(view: TextView, duration: Int) {
    val min = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
    val sec = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(min)
    view.text = "%d:%02d".format(min, sec)
}