package com.neeplayer.api.apotify

import com.neeplayer.api.Response
import com.neeplayer.api.Response.Success
import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import javax.inject.Inject


interface SpotifyApi {
    companion object {
        const val token = "BQCWhkZIULuYO_kovCbqUEM5RIuMWIH-K84h3Ez4XLtwp1ZPHH9cygP2uFEDcUVill5yl8eIWPcdM41g0bBxEsJ9qqRU7mZc02EFS5drID2vRf0C2W6AN8IIBox-OedA-6FADAdJvQKS2AmKjTBnN7WjO6TUQ9c46w"
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

    override suspend fun searchArtist(artistName: String) = deezerRequest<SpotifyApi.SearchBody> {
        url {
            encodedPath += "/search"
            parameters.append("q", artistName)
            parameters.append("type", "artist")
            parameters.append("limit", 1.toString())
        }
    }

    private suspend inline fun <reified T> deezerRequest(
            block: HttpRequestBuilder.() -> Unit
    ): Response<T> {
        return try {
            httpClient.request<T> {
                url.takeFrom(baseUrlBuilder)
                header("Authorization", "Bearer ${SpotifyApi.token}")
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

