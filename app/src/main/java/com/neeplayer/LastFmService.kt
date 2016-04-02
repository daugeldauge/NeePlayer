package com.neeplayer

import org.json.JSONObject
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import rx.Single

interface LastFmService {

    companion object {
        const val apiKey = "76b52a83c8c82ae436524353bcea2da0"
        const val secret = "4ce7ed189d2c08ae5091003a8e81f6d5"
    }

    @GET("?method=auth.getToken")
    fun getToken(): Single<JSONObject>

    @GET("?method=auth.getSession")
    fun getSession(@Query("token") token: String): Single<JSONObject>

    @GET("?method=artist.getInfo")
    fun getArtistInfo(@Query("artist") artist: String): Single<JSONObject>

    @POST("?method=track.scrobble")
    fun scrobble(
            @Query("sk") sessionKey: String,
            @Query("artist") artist: String,
            @Query("track") track: String,
            @Query("timestamp") timestamp: Long,
            @Query("album") album: String? = null
    ): Single<JSONObject>
}

