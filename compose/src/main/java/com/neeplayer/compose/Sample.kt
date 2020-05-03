package com.neeplayer.compose

import androidx.activity.ComponentActivity
import androidx.ui.core.setContent

object Sample {
    val artists = listOf(
            Artist(0, "David Bowie", 10, 2, "https://1.bp.blogspot.com/_jKzgD4pKhNk/TBlyvk3XIgI/AAAAAAAAB9U/5N0Y6UGFH4g/s400/bowieprep1.jpg"),
            Artist(0, "Yes", 10, 2, "https://i.ytimg.com/vi/UliAuTWNTlg/hqdefault.jpg"),
            Artist(0, "Grateful Dead", 10, 2, "https://www8.indirimglobal.co/26999-1-home_default/Gratel-ölü-psychedelic-rock-grubu-jerry-garcia-poster-ipek-kumaş-bez-baskı-duvar-sticker-duvar-dekorasyonu-özel-baskı.jpg"),
            Artist(0, "Joy Division", 10, 2, "https://citaty.info/files/posters/158847.jpg")
    ).repeat(50)

    private val songs = listOf(
            Song(0, "Sound and Vision (Remaster, reprise and other things)", 350_000, 1, false),
            Song(0, "Promises", 350_000, 2, false),
            Song(0, "Animal Instinct", 350_000, 3, true),
            Song(0, "Linger", 12_000, 4, false),
            Song(0, "Heart of Gold", 234_000, 5, false)
    )

    val albums = listOf(
                    Album(0, artists[0], "Low", 1970, null, 10, -100),
                    Album(0, artists[1], "Fragile", null, null, 2, 10_000_000),
                    Album(0, artists[2], "Wake of the Flood", 1970, null, 10, 312_000)
    ).map { AlbumWithSongs(it, songs ) }.repeat(10)
}

fun ComponentActivity.setSampleContent() {
    setContent {
        PreviewAlbumsScreen()
    }
}

private fun <T> Iterable<T>.repeat(n: Int) = List(n) { this }.flatten()