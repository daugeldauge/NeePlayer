package com.neeplayer.api.lastfm

import com.neeplayer.BuildConfig
import com.neeplayer.api.lastfm.LastFmApi.GetTokenResponse
import com.neeplayer.api.lastfm.LastFmApi.Result.Success
import com.neeplayer.di.LastFmModule
import com.neeplayer.fold
import com.neeplayer.md5
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.ResponseException
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json.Companion.nonstrict
import timber.log.Timber
import javax.inject.Inject


interface LastFmApi {

    sealed class Result<out T> {
        class Success<T>(val data: T) : Result<T>()
        object Error : Result<Nothing>()
    }

    @Serializable
    class GetTokenResponse(val token: String)

    @Serializable
    class GetSessionResponse(val session: Session) {
        @Serializable
        class Session(val key: String)
    }

    suspend fun getToken(): Result<GetTokenResponse>

    suspend fun getSession(token: String): Result<GetSessionResponse>

    suspend fun scrobble(
            sessionKey: String,
            artist: String,
            track: String,
            timestamp: Long,
            album: String? = null
    ): Result<*>
}

class LastFmKtorApi @Inject constructor() : LastFmApi {

    private val baseUrlBuilder = URLBuilder("https://ws.audioscrobbler.com/2.0")

    @UseExperimental(UnstableDefault::class)
    private val httpClient = HttpClient(Android) {
        Json {
            serializer = KotlinxSerializer(nonstrict)
        }

        if (BuildConfig.DEBUG) {
            Logging {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("ktor").i(message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    override suspend fun getToken() = lastFmRequest<GetTokenResponse>("auth.getToken")

    override suspend fun getSession(token: String) = lastFmRequest<LastFmApi.GetSessionResponse>("auth.getSession") {
        append("token", token)
    }

    override suspend fun scrobble(
            sessionKey: String,
            artist: String,
            track: String,
            timestamp: Long,
            album: String?
    ) = lastFmRequest<HttpResponse>("track.scrobble", HttpMethod.Post) {

    }

    private suspend inline fun <reified T> lastFmRequest(
            method: String,
            httpMethod: HttpMethod = HttpMethod.Get,
            parametersConfiguration: ParametersBuilder.() -> Unit = {}
    ): LastFmApi.Result<T> {
        return try {
            httpClient.request<T> {
                this.method = httpMethod
                url.takeFrom(baseUrlBuilder)
                url.parameters.apply {
                    append("api_key", LastFmModule.apiKey)
                    append("method", method)
                    parametersConfiguration()
                    append("api_sig", calculateSign())
                    append("format", "json")
                }
            }.let(::Success)
        } catch (e: IOException) {
            LastFmApi.Result.Error
        } catch (e: SerializationException) {
            LastFmApi.Result.Error
        } catch (e: ResponseException) {
            LastFmApi.Result.Error
        }
    }

    private fun ParametersBuilder.calculateSign(): String {
        return entries()
                .associate { (key, value) -> key to value.firstOrNull() }
                .calculateSign()
    }

    private fun Map<String, String?>.calculateSign(): String {
        return toSortedMap()
                .filter { !it.key.isNullOrBlank() && !it.value.isNullOrBlank() }
                .fold(StringBuilder()) { accumulator, key, value -> accumulator.append(key).append(value) }
                .append(LastFmModule.secret)
                .toString()
                .md5()
    }
}

