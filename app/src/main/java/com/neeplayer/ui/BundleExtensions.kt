package com.neeplayer.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlin.reflect.KProperty

fun <T: Parcelable> Fragment.putArgument(property: KProperty<T>, value: T) {
    (arguments ?: Bundle().also { arguments = it }).putParcelable(property.name, value)
}

operator fun <T : Parcelable> (() -> Bundle?).getValue(thisRef: Any?, property: KProperty<*>): T = this()!!.getParcelable(property.name)!!