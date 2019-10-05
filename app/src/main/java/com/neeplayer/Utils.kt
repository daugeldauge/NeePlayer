package com.neeplayer

import android.content.Context
import android.widget.Toast
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit


fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5").digest(this.toByteArray(charset("UTF-8")))
    return String.format("%032x", BigInteger(1, digest))
}

inline fun <K, V, R> Map<K, V>.fold(initial: R, operation: (R, K, V) -> R): R {
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element.key, element.value)
    }
    return accumulator
}

val Int.minutes: Long
    get() = TimeUnit.MINUTES.toMillis(toLong())

val Int.seconds: Long
    get() = TimeUnit.SECONDS.toMillis(toLong())

fun Context.toast(textResource: Int) {
    Toast.makeText(this, textResource, Toast.LENGTH_SHORT).show()
}

fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}