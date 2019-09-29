package com.neeplayer.api

sealed class Response<out T> {
    class Success<T>(val body: T) : Response<T>()
    object Error : Response<Nothing>()
}