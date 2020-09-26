package com.neeplayer.compose

data class Artist(
    val id: Long,
    val name: String,
    val numberOfSongs: Int,
    val numberOfAlbums: Int,
    val imageUrl: String? = null
)

sealed class Composition

data class Album(
    val id: Long,
    val title: String?,
    val year: Int?,
    val art: String?,
    val numberOfSongs: Int,
    val duration: Long
) : Composition()

data class Song(
    val id: Long,
    val title: String?,
    val duration: Long,
    val track: Int?
) : Composition()

data class AlbumWithSongs(
    val album: Album,
    val songs: List<Song>
)