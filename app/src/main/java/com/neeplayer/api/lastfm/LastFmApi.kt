package com.neeplayer.api.lastfm

import com.neeplayer.BuildConfig
import com.neeplayer.api.Response
import com.neeplayer.api.Response.Success
import com.neeplayer.api.lastfm.LastFmApi.GetTokenBody
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

    companion object {
        const val apiKey = "76b52a83c8c82ae436524353bcea2da0"
        const val secret = "4ce7ed189d2c08ae5091003a8e81f6d5"
    }

    @Serializable
    class GetTokenBody(val token: String)

    @Serializable
    class GetSessionBody(val session: Session) {
        @Serializable
        class Session(val key: String)
    }

    suspend fun getToken(): Response<GetTokenBody>

    suspend fun getSession(token: String): Response<GetSessionBody>

    suspend fun scrobble(
            sessionKey: String,
            artist: String,
            track: String,
            timestamp: Long,
            album: String? = null
    ): Response<*>
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
                level = LogLevel.BODY
            }
        }
    }

    override suspend fun getToken() = lastFmRequest<GetTokenBody>("auth.getToken")

    override suspend fun getSession(token: String) = lastFmRequest<LastFmApi.GetSessionBody>("auth.getSession") {
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
    ): Response<T> {
        return try {
            httpClient.request<T> {
                this.method = httpMethod
                url.takeFrom(baseUrlBuilder)
                url.parameters.apply {
                    append("api_key", LastFmApi.apiKey)
                    append("method", method)
                    parametersConfiguration()
                    append("api_sig", calculateSign())
                    append("format", "json")
                }
            }.let(::Success)
        } catch (e: IOException) {
            Response.Error
        } catch (e: SerializationException) {
            Response.Error
        } catch (e: ResponseException) {
            Response.Error
        }
    }

    private fun ParametersBuilder.calculateSign(): String {
        return entries()
                .associate { (key, value) -> key to value.firstOrNull() }
                .toSortedMap()
                .filter { !it.key.isNullOrBlank() && !it.value.isNullOrBlank() }
                .fold(StringBuilder()) { accumulator, key, value -> accumulator.append(key).append(value) }
                .append(LastFmApi.secret)
                .toString()
                .md5()
    }
}

