package com.neeplayer.network.spotify

import com.neeplayer.network.Response
import com.neeplayer.network.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable

interface SpotifyApi {
    companion object {
        const val token = "BQAb4uHYGY-VCdVI_nwaAsFdfMYZIO6pxxSM1-W6TbIVPkW4StR2W6mlbfboCV-BgiBAfhbGbdWQVQQ-fFeKWCY7hRT-BsRfPuWM5bavMFAew5T5lzN0Xw0ms_mxtQPkzF9t2EJgr6xgn3PyUE_8t-nelcpY4CU"
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

class SpotifyKtorApi(private val httpClient: HttpClient) : SpotifyApi {

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

