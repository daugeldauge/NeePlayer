package com.neeplayer.di

import com.neeplayer.BuildConfig
import com.neeplayer.network.deezer.DeezerApi
import com.neeplayer.network.deezer.DeezerKtorApi
import com.neeplayer.network.lastfm.LastFmApi
import com.neeplayer.network.lastfm.LastFmKtorApi
import com.neeplayer.network.spotify.SpotifyApi
import com.neeplayer.network.spotify.SpotifyKtorApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.UnstableDefault
import org.koin.dsl.module
import timber.log.Timber

val networkModule = module {
    single { createKtorHttpClient() }
    single<LastFmApi> { LastFmKtorApi(get()) }
    single<DeezerApi> { DeezerKtorApi(get()) }
    single<SpotifyApi> { SpotifyKtorApi(get()) }
}

@UseExperimental(UnstableDefault::class)
fun createKtorHttpClient() = HttpClient(Android) {
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