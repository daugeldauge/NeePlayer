package com.neeplayer.model

import org.json.JSONObject
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import rx.Single

interface LastFmService {

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

