package com.neeplayer.model

import android.content.Context
import android.content.SharedPreferences
import com.neeplayer.model.Preferences.Item.StringItem.SESSION_KEY
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor (context: Context) {

    private val preferences = context.getSharedPreferences("main", Context.MODE_PRIVATE)

    sealed class Item<T> {
        internal val key = javaClass.simpleName

        abstract fun put(editor: SharedPreferences.Editor, value: T)
        abstract fun get(preferences: SharedPreferences): T?

        sealed class StringItem : Item<String>() {
            override fun put(editor: SharedPreferences.Editor, value: String) { editor.putString(key, value) }
            override fun get(preferences: SharedPreferences) = preferences.getString(key, null)

            object SESSION_KEY: StringItem()
        }

        sealed class LongItem : Item<Long>() {
            override fun put(editor: SharedPreferences.Editor, value: Long) { editor.putLong(key, value) }
            override fun get(preferences: SharedPreferences) = if (preferences.contains(key)) preferences.getLong(key, 0) else null

            object NOW_PLAYING_SONG_ID: LongItem()
        }

        sealed class BooleanItem(override val default: Boolean) : Item<Boolean>(), ItemWithDefaultValue<Boolean> {

            override fun put(editor: SharedPreferences.Editor, value: Boolean) { editor.putBoolean(key, value) }
            override fun get(preferences: SharedPreferences): Boolean? = if (preferences.contains(key)) preferences.getBoolean(key, false) else null

            object SCROBBLING : BooleanItem(default = true)
        }
    }

    interface ItemWithDefaultValue<T> {
        val default: T
    }

    fun isSignedIn() = get(SESSION_KEY) != null

    fun <T> put(item: Item<T>, value: T) {
        val editor = preferences.edit()
        item.put(editor, value)
        editor.apply()
    }

    fun <T> remove(item: Item<T>) {
        val editor = preferences.edit()
        editor.remove(item.key)
        editor.apply()
    }

    fun <T> get(item: Item<T>): T? = item.get(preferences)

    fun <T, I> getOrDefault(item: I): T where I: Item<T>, I: ItemWithDefaultValue<T> = get(item) ?: item.default
}