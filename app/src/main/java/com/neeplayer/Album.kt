package com.neeplayer

import java.io.Serializable

class Album(val id: Long, val title: String?, val year: Int?, val art: String?, val songs: List<Song>) : Serializable
