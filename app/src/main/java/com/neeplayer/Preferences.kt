package com.neeplayer

import android.content.Context
import android.content.SharedPreferences

class Preferences(context: Context) {

    private val preferences = context.getSharedPreferences("main", Context.MODE_PRIVATE)

    sealed class Item<T> {
        protected val key = javaClass.simpleName

        abstract fun put(editor: SharedPreferences.Editor, value: T)
        abstract fun get(preferences: SharedPreferences): T?

        sealed class StringItem : Item<String>() {
            override fun put(editor: SharedPreferences.Editor, value: String) { editor.putString(key, value) }
            override fun get(preferences: SharedPreferences) = preferences.getString(key, null)

            object SESSION_KEY: StringItem()
        }

        sealed class LongItem : Item<Long>() {
            override fun put(editor: SharedPreferences.Editor, value: Long) { editor.putLong(key, value) }
            override fun get(preferences: SharedPreferences): Long? {
                val value = preferences.getLong(key, Long.MIN_VALUE)
                return if (value != Long.MIN_VALUE) value else null
            }

            object NOW_PLAYING_SONG_ID: LongItem()
        }

    }


    fun <T> put(item: Item<T>, value: T) {
        val editor = preferences.edit()
        item.put(editor, value)
        editor.apply()
    }

    fun <T> get(item: Item<T>): T? = item.get(preferences)
}