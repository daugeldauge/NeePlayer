package com.neeplayer

import android.content.Context

class Preferences(context: Context) {

    private val preferences = context.getSharedPreferences("main", Context.MODE_PRIVATE)

    enum class Item {
        SESSION_KEY
    }

    fun put(item: Item, value: String) {
        preferences.edit().putString(item.name, value).apply();
    }

    fun get(item: Item): String? = preferences.getString(item.name, null);

}