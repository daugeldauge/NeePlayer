package com.neeplayer.model

import android.content.Context

object Model {
    private val SHARED_PREFERENCES_NAME = "MAIN"
    private val NOW_PLAYING_SONG = "NOW_PLAYING_SONG"

    private lateinit var context: Context

    private val prefs by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var nowPlaying: Playlist? = null
    set(value) {
        val oldValue = field
        field = value
        if (oldValue != value) {
            progress = 0
            nowPlayingListeners.forEach { it() }
        }
    }

    var paused = true
    set(value) {
        field = value
        nowPlayingListeners.forEach { it() }
    }

    private val nowPlayingListeners = mutableSetOf<() -> Unit>()

    var progress = 0
    set(value) {
        field = value
        progressListeners.forEach { it() }
    }

    private val progressListeners = mutableSetOf<() -> Unit>()

    fun addNowPlayingListener(listener: () -> Unit) {
        listener()
        nowPlayingListeners.add(listener)
    }

    fun addProgressListener(listener: () -> Unit) {
        listener()
        progressListeners.add(listener)
    }

    fun removeNowPlayingListener(listener: () -> Unit) {
        nowPlayingListeners.remove(listener)
    }

    fun removeProgressListener(listener: () -> Unit) {
        progressListeners.remove(listener)
    }


    fun init(context: Context) {
        this.context = context.applicationContext
        Database.init(context)

        val nowPlayingSongId = prefs.getLong(NOW_PLAYING_SONG, -1L)
        if (nowPlayingSongId == -1L) {
            return
        }

        nowPlaying = Database.restorePlaylist(nowPlayingSongId)
    }

    fun save() {
        val nowPlaying = nowPlaying ?: return
        prefs.edit().putLong(NOW_PLAYING_SONG, nowPlaying.currentSong.id).apply()
    }
}