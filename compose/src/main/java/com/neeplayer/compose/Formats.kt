package com.neeplayer.compose

import java.util.concurrent.TimeUnit

fun Long?.formatDuration(): String {
    val duration = this ?: 0
    val min = TimeUnit.MILLISECONDS.toMinutes(duration)
    val sec = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(min)
    return "%d:%02d".format(min, sec)
}
