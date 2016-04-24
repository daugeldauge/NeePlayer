package com.neeplayer.model

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistImagesStorage @Inject constructor (context: Context) {

    private val preferences by lazy {
        context.getSharedPreferences("ArtistImages", 0)
    }

    fun get(name: String): String? = preferences.getString(name, null)

    fun put(name: String, imageUrl: String) {
        preferences.edit().putString(name, imageUrl).apply()
    }
}