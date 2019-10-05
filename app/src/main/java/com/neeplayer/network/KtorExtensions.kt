package com.neeplayer.network

import com.neeplayer.network.Response.Success
import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> HttpClient.safeRequest(block: HttpRequestBuilder.() -> Unit): Response<T> {
    return try {
        request<T>(block).let(::Success)
    } catch (e: IOException) {
        Response.Error
    } catch (e: SerializationException) {
        Response.Error
    } catch (e: ResponseException) {
        Response.Error
    }
}
