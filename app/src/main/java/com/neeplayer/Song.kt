package com.neeplayer

import java.io.Serializable

class Song(val id: Long, val title: String?, val duration: Long, val track: Int?) : Serializable
