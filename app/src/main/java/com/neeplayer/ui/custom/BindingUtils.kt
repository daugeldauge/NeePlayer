package com.neeplayer.ui.custom

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.neeplayer.R
import java.util.concurrent.TimeUnit

@BindingAdapter("bind:imagePath")
fun loadImageFile(view: ImageView, path: String?) = loadImage(view, "file://" + path)

@BindingAdapter("bind:imageURL")
fun loadImage(view: ImageView, url: String?) = Glide.with(view.context).load(url).dontAnimate().into(view)

@BindingAdapter("bind:drawable")
fun setDrawable(view: ImageView, drawable: Drawable) = view.setImageDrawable(drawable)

@BindingAdapter("bind:animatedVectorDrawable")
fun setAnimatedVectorDrawable(view: ImageView, name: String) =
        view.setImageDrawable(AnimatedVectorDrawableCompat.create(view.context, R::class.java.getField(name).getInt(-1)))

@BindingAdapter("bind:duration")
fun setFormattedDuration(view: TextView, duration: Int) {
    val min = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
    val sec = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(min)
    view.text = "%d:%02d".format(min, sec)
}