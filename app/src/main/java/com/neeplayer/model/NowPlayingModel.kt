package com.neeplayer.model

import android.content.Context
import com.neeplayer.*
import com.neeplayer.Preferences.Item.LongItem.*
import com.neeplayer.Preferences.Item.StringItem.*
import org.jetbrains.anko.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class NowPlayingModel(val context: Context, val lastFm: LastFmService, val preferences: Preferences, val database: Database) {

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


    init {
        val nowPlayingSongId = preferences.get(NOW_PLAYING_SONG_ID)

        if (nowPlayingSongId != null) {
            nowPlaying = database.restorePlaylist(nowPlayingSongId)
        }
    }

    fun save() {
        val nowPlaying = nowPlaying ?: return
        preferences.put(NOW_PLAYING_SONG_ID, nowPlaying.currentSong.id)
    }

    fun scrobble() {
        val song = (nowPlaying?.currentSong) ?: return

        lastFm.scrobble(
                track = song.title ?: return,
                album = song.album.title,
                artist = song.album.artist.name,
                timestamp = System.currentTimeMillis() / 1000,
                sessionKey = preferences.get(SESSION_KEY) ?: return
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    context.toast(context.getString(R.string.scrobble_success, song.title))
                }, {
                    context.toast(R.string.scrobble_error)
                })
    }
}