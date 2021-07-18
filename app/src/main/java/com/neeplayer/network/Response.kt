package com.neeplayer.network

sealed class Response<out T> {
    class Success<T>(val body: T) : Response<T>()
    object Error : Response<Nothing>()

    fun success(): Success<out T>? = this as? Success<out T>
}
