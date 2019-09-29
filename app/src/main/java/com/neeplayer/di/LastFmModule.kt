package com.neeplayer.di

import com.neeplayer.api.lastfm.LastFmApi
import com.neeplayer.api.lastfm.LastFmKtorApi
import dagger.Binds
import dagger.Module

@Module
abstract class LastFmModule {
    @Binds
    abstract fun bindApi(ktorApi: LastFmKtorApi): LastFmApi
}