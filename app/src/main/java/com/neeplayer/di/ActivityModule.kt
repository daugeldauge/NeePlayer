package com.neeplayer.di

import com.neeplayer.ui.MainActivity
import com.neeplayer.ui.Router
import com.neeplayer.ui.artists.ArtistsPresenter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val activityModule = module {
    scope(named<MainActivity>()) {
        scoped { Router(get()) }
        factory { ArtistsPresenter(get(), get(), get(), get(), get()) }
    }
}
