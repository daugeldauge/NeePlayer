package com.neeplayer.compose

data class Artist(
        val id: Long,
        val name: String,
        val numberOfSongs: Int,
        val numberOfAlbums: Int,
        val imageUrl: String? = null
)

data class Album(
        val id: Long,
        val artist: Artist,
        val title: String?,
        val year: Int?,
        val art: String?
)

data class Song(
        val id: Long,
        val title: String?,
        val duration: Int,
        val track: Int?
)

data class AlbumWithSongs(
        val album: Album,
        val songs: List<Song>
)