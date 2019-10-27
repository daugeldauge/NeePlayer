package com.neeplayer.di

import android.app.Application
import com.neeplayer.model.*
import com.neeplayer.ui.albums.AlbumsPresenter
import com.neeplayer.ui.auth.AuthPresenter
import com.neeplayer.ui.now_playing.NowPlayingPresenter
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import org.koin.dsl.module

val appModule = module {

    single<StorIOContentResolver> {
        DefaultStorIOContentResolver.builder()
                .contentResolver(get<Application>().contentResolver)
                .build()
    }

    single { ArtistImagesStorage(get()) }
    single { ArtistResolver(get()) }
    single { Database(get(), get()) }
    single { Preferences(get()) }
    single { NowPlayingService(get(), get()) }
    single { Scrobbler(get(), get(), inject(), get()) }

    factory { AlbumsPresenter(get(), get()) }
    factory { NowPlayingPresenter(get()) }
    factory { AuthPresenter(get(), get()) }

}
