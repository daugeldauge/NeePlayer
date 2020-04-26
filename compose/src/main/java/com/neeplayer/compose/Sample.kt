package com.neeplayer.compose

import androidx.activity.ComponentActivity
import androidx.ui.core.setContent

object Sample {
    val artists = listOf(
            Artist(0, "David Bowie", 10, 2, "https://1.bp.blogspot.com/_jKzgD4pKhNk/TBlyvk3XIgI/AAAAAAAAB9U/5N0Y6UGFH4g/s400/bowieprep1.jpg"),
            Artist(0, "Yes", 10, 2, "https://i.ytimg.com/vi/UliAuTWNTlg/hqdefault.jpg"),
            Artist(0, "Grateful Dead", 10, 2, "https://www8.indirimglobal.co/26999-1-home_default/Gratel-ölü-psychedelic-rock-grubu-jerry-garcia-poster-ipek-kumaş-bez-baskı-duvar-sticker-duvar-dekorasyonu-özel-baskı.jpg"),
            Artist(0, "Joy Division", 10, 2, "https://citaty.info/files/posters/158847.jpg")
    ).let { list -> List(50) { list }.flatten() }
}

fun ComponentActivity.setSampleContent() {
    setContent {
        PreviewArtistsScreen()
    }
}