package com.neeplayer

import java.math.BigInteger
import java.security.MessageDigest
import java.util.*


fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5").digest(this.toByteArray(charset("UTF-8")))
    return String.format("%032x", BigInteger(1, digest))
}

fun indexRange(size: Int): IntRange = 0.rangeTo(size - 1)

inline fun <T, K : Comparable<K>, V> Iterable<T>.toSortedMap(transform: (T) -> Pair<K, V>): SortedMap<K, V> = this.map(transform).toMap().toSortedMap()

inline fun <K, V, R> Map<K, V>.fold(initial: R, operation: (R, K, V) -> R): R {
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element.key, element.value)
    }
    return accumulator
}
