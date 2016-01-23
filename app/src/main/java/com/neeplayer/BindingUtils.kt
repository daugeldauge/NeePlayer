package com.neeplayer

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

@BindingAdapter("bind:imagePath")
fun loadImageFile(view: ImageView, path: String) = loadImage(view, "file://" + path)

@BindingAdapter("bind:imageURL")
fun loadImage(view: ImageView, url: String) = Glide.with(view.getContext()).load(url).dontAnimate().into(view)
