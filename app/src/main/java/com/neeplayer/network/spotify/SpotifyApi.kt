package com.neeplayer.network.spotify

import com.neeplayer.network.Response
import com.neeplayer.network.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable
import javax.inject.Inject


interface SpotifyApi {
    companion object {
        const val token = "BQBo7BQDkAqdQs2kSaLlA0SJSTNPpq6FAxJKaueiPMdjApGw1ktD-mcLzhiQU0Vk7JZY-TH0Jl7HLVIIC10g1akaqB362rL9t4jRPpy4q2EvIZ8kT_1-YBsODh8BUcYQRkrJ9fU8a9n8TOoRpG3JhN8S_T1ZxidfyQ"
    }

    @Serializable
    class SearchBody(val artists: Artists) {
        @Serializable
        class Artists(val items: List<Artist>)
    }

    @Serializable
    class Artist(val images: List<Image>) {
        @Serializable
        class Image(val url: String)
    }

    suspend fun searchArtist(artistName: String): Response<SearchBody>
}

class SpotifyKtorApi @Inject constructor(private val httpClient: HttpClient) : SpotifyApi {

    private val baseUrlBuilder = URLBuilder("https://api.spotify.com/v1")

    override suspend fun searchArtist(artistName: String) = spotifyRequest<SpotifyApi.SearchBody> {
        url {
            encodedPath += "/search"
            parameters.append("q", artistName)
            parameters.append("type", "artist")
            parameters.append("limit", 1.toString())
        }
    }

    private suspend inline fun <reified T> spotifyRequest(
            block: HttpRequestBuilder.() -> Unit
    ): Response<T> {
        return httpClient.safeRequest {
            url.takeFrom(baseUrlBuilder)
            header("Authorization", "Bearer ${SpotifyApi.token}")
            block()
        }
    }
}

