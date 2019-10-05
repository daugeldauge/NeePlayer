package com.neeplayer.network.deezer

import com.neeplayer.network.Response
import com.neeplayer.network.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLProtocol
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject


interface DeezerApi {

    @Serializable
    class SearchBody(val data: List<SearchData>) {
        @Serializable
        class SearchData(val artist: Artist)
    }

    @Serializable
    class Artist(@SerialName("picture_medium") val pictureMedium: String)

    suspend fun searchArtist(artistName: String): Response<SearchBody>
}

class DeezerKtorApi @Inject constructor(private val httpClient: HttpClient) : DeezerApi {

    override suspend fun searchArtist(artistName: String) = deezerRequest<DeezerApi.SearchBody> {
        url {
            encodedPath = "/search"
            parameters.append("q", "artist:\"$artistName\"")
            parameters.append("limit", 1.toString())
        }
    }

    private suspend inline fun <reified T> deezerRequest(
            block: HttpRequestBuilder.() -> Unit
    ): Response<T> {
        return httpClient.safeRequest {
            url.protocol = URLProtocol.HTTPS
            url.host = "api.deezer.com"
            block()
        }
    }
}

