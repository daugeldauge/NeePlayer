package com.neeplayer.ui

import android.os.Bundle
import android.os.Parcelable
import kotlin.reflect.KProperty

operator fun <T : Parcelable> Bundle.getValue(thisRef: Any?, property: KProperty<*>): T = getParcelable(property.name)!!

operator fun <T : Parcelable> Bundle.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    putParcelable(property.name, value)
}