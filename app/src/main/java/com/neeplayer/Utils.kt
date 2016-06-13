package com.neeplayer

import android.os.Build
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.math.BigInteger
import java.security.MessageDigest
import java.util.SortedMap
import java.util.concurrent.TimeUnit
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty


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

inline fun <R> targetApi(api: Int, success: () -> R, failure: () -> R) =
        if (Build.VERSION.SDK_INT >= api) {
            success()
        } else {
            failure()
        }

inline fun targetApi(api: Int, block: () -> Unit) =
        targetApi(api, block) {}

inline fun <R> lollipop(success: () -> R, failure: () -> R) =
        targetApi(Build.VERSION_CODES.LOLLIPOP, success, failure)

inline fun lollipop(block: () -> Unit) =
        lollipop(block) {}

operator fun CompositeSubscription.plusAssign(subscription: Subscription) = add(subscription)

val Int.minutes: Long
    get() = TimeUnit.MINUTES.toMillis(toLong())

val Int.seconds: Long
    get() = TimeUnit.SECONDS.toMillis(toLong())

class RxProperty<T>(initial: T, private val observer: Observer<T>) : ObservableProperty<T>(initial) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        observer.onNext(newValue)
    }
}

inline fun <reified T: Any> Observable<T?>.filterNotNull(): Observable<T> {
    return filter { it != null }.cast(T::class.java)
}

inline fun <T> T?.ifPresent(block: (T) -> Unit) {
    if (this != null) {
        block(this)
    }
}