package com.neeplayer

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

@BindingAdapter("bind:imagePath")
fun loadImage(view: ImageView, path: String) = Glide.with(view.getContext()).load("file://" + path).dontAnimate().into(view)
