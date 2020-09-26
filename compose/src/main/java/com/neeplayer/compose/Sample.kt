package com.neeplayer.compose

object Sample {
    val artists = listOf(
        Artist(0, "David Bowie", 10, 2, "https://1.bp.blogspot.com/_jKzgD4pKhNk/TBlyvk3XIgI/AAAAAAAAB9U/5N0Y6UGFH4g/s400/bowieprep1.jpg"),
        Artist(1, "Yes", 10, 2, "https://i.ytimg.com/vi/UliAuTWNTlg/hqdefault.jpg"),
        Artist(2, "Grateful Dead", 10, 2, "https://www8.indirimglobal.co/26999-1-home_default/Gratel-ölü-psychedelic-rock-grubu-jerry-garcia-poster-ipek-kumaş-bez-baskı-duvar-sticker-duvar-dekorasyonu-özel-baskı.jpg"),
        Artist(3, "Joy Division", 10, 2, "https://citaty.info/files/posters/158847.jpg"),
    ).repeat(50)

    val songs = listOf(
        Song(0, "Sound and Vision (Remaster, reprise and other things)", 350_000, 1),
        Song(1, "Promises", 350_000, 2),
        Song(2, "Animal Instinct", 350_000, 3),
        Song(3, "Linger", 12_000, 4),
        Song(4, "Heart of Gold", 234_000, 5),
    )

    val albums = listOf(
        Album(1, "Low", 1970, artists[0].imageUrl, 10, -100),
        Album(2, "Fragile", null, artists[1].imageUrl, 2, 10_000_000),
        Album(3, "Wake of the Flood", 1970, artists[2].imageUrl, 10, 312_000),
    ).map { AlbumWithSongs(it, songs) }.repeat(10)
}

private fun <T> Iterable<T>.repeat(n: Int) = List(n) { this }.flatten()