package com.neeplayer

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

interface LastFmService {

    @GET("2.0?method=artist.getinfo&format=json")
    fun getArtistInfo(
            @Query("artist") artist: String,
            @Query("api_key") apiKey: String = "76b52a83c8c82ae436524353bcea2da0"
    ): Observable<ResponseBody>
}