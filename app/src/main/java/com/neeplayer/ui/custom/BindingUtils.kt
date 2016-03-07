package com.neeplayer.ui.custom

import android.databinding.BaseObservable
import android.databinding.BindingAdapter
import android.databinding.ObservableField
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.jetbrains.anko.imageBitmap
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty

@BindingAdapter("bind:imagePath")
fun loadImageFile(view: ImageView, path: String?) {
    loadImage(view, "file://" + path)
    return
//    path ?: return
//
//    val sampleSize = calculateSampleSize(path, view.width, view.height)
//    val tag = path + "#" + sampleSize.toString()
//
//    if (tag.equals(view.tag)) {
//        return
//    }
//
//    view.tag = tag
//    val options = BitmapFactory.Options()
//    options.inSampleSize = sampleSize
//    view.imageBitmap = BitmapFactory.decodeFile(path, options)
}

// http://developer.android.com/intl/ru/training/displaying-bitmaps/load-bitmap.html
fun calculateSampleSize(path: String, reqWidth: Int, reqHeight: Int): Int {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val height = options.outHeight
    val width = options.outWidth
    var sampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight = height / 2
        val halfWidth = width / 2

        while ((halfHeight / sampleSize) > reqHeight && (halfWidth / sampleSize) > reqWidth) {
            sampleSize *= 2
        }
    }

    return sampleSize
}

@BindingAdapter("bind:imageURL")
fun loadImage(view: ImageView, url: String?) = Glide.with(view.context).load(url).dontAnimate().into(view)

@BindingAdapter("bind:drawable")
fun setDrawable(view: ImageView, drawable: Drawable) = view.setImageDrawable(drawable)

@BindingAdapter("bind:duration")
fun setFormattedDuration(view: TextView, duration: Long) {
    val min = TimeUnit.MILLISECONDS.toMinutes(duration)
    val sec = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(min)
    view.text = "%d:%02d".format(min, sec)
}

class ObservableDelegate<T>(initialValue: T, vararg val dependants: Int) {
    private val observable: ObservableField<T> = ObservableField(initialValue)

    operator fun getValue(model: BaseObservable, property: KProperty<*>): T = observable.get()

    operator fun setValue(model: BaseObservable, property: KProperty<*>, value: T)  {
        observable.set(value)

        dependants.forEach {
            model.notifyPropertyChanged(it)
        }
    }
}