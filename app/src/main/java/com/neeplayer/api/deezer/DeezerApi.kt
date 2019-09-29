package com.neeplayer.api.deezer

import com.neeplayer.api.Response
import com.neeplayer.api.Response.Success
import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.URLProtocol
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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
        return try {
            httpClient.request<T> {
                url.protocol = URLProtocol.HTTPS
                url.host = "api.deezer.com"
                block()
            }.let(::Success)
        } catch (e: IOException) {
            Response.Error
        } catch (e: SerializationException) {
            Response.Error
        } catch (e: ResponseException) {
            Response.Error
        }
    }
}

