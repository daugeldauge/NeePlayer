package com.neeplayer.di

import com.neeplayer.fold
import com.neeplayer.indexRange
import com.neeplayer.md5
import com.neeplayer.model.LastFmService
import com.neeplayer.toSortedMap
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import java.lang.reflect.Type
import javax.inject.Singleton

@Module
class LastFmModule {

    companion object {
        const val apiKey = "76b52a83c8c82ae436524353bcea2da0"
        const val secret = "4ce7ed189d2c08ae5091003a8e81f6d5"
    }

    @Provides
    @Singleton
    fun provideLastFmService(): LastFmService {
        val lastFmInterceptor = Interceptor { chain ->
            val url = chain.request()
                    .url()
                    .newBuilder()
                    .addQueryParameter("api_key", apiKey)
                    .build()

            val signedUrl = url.newBuilder()
                    .addQueryParameter("api_sig", calculateSign(url))
                    .addQueryParameter("format", "json")
                    .build()

            chain.proceed(chain.request().newBuilder().url(signedUrl).build())
        }

        val httpClient = OkHttpClient.Builder()
                .addInterceptor(lastFmInterceptor)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();

        return Retrofit.Builder()
                .baseUrl("http://ws.audioscrobbler.com/2.0/")
                .addConverterFactory(object : Converter.Factory() {
                    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, JSONObject> {
                        return Converter { body -> JSONObject(body.string()) }
                    }
                })
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build()
                .create(LastFmService::class.java)

    }

    private fun calculateSign(url: HttpUrl): String =
            indexRange(url.querySize())
                    .toSortedMap { url.queryParameterName(it) to url.queryParameterValue(it) }
                    .filter { !it.key.isNullOrBlank() && !it.value.isNullOrBlank() }
                    .fold(StringBuilder()) { accumulator, key, value -> accumulator.append(key).append(value) }
                    .append(secret)
                    .toString()
                    .md5()
}