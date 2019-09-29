package com.neeplayer.di

import com.neeplayer.BuildConfig
import com.neeplayer.api.deezer.DeezerApi
import com.neeplayer.api.deezer.DeezerKtorApi
import com.neeplayer.api.lastfm.LastFmApi
import com.neeplayer.api.lastfm.LastFmKtorApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.UnstableDefault
import timber.log.Timber
import javax.inject.Singleton

@Module(includes = [NetworkModule.Bindings::class])
object NetworkModule {
    @Singleton
    @Provides
    @JvmStatic
    @UseExperimental(UnstableDefault::class)
    fun provideKtorHttpClient() = HttpClient(Android) {
        Json {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json.nonstrict)
        }

        if (BuildConfig.DEBUG) {
            Logging {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("ktor").i(message)
                    }
                }
                level = LogLevel.BODY
            }
        }
    }

    @Module
    abstract class Bindings {
        @Binds
        @Singleton
        abstract fun bindLastFm(ktorApi: LastFmKtorApi): LastFmApi

        @Binds
        @Singleton
        abstract fun bindDeezer(ktorApi: DeezerKtorApi): DeezerApi
    }
}