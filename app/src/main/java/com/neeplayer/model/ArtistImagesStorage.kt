package com.neeplayer.model

import android.app.Application

class ArtistImagesStorage(context: Application) {

    private val preferences by lazy {
        context.getSharedPreferences("ArtistImages", 0)
    }

    fun get(name: String): String? = preferences.getString(name, null)

    fun put(name: String, imageUrl: String) {
        preferences.edit().putString(name, imageUrl).apply()
    }
}