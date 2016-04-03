package com.neeplayer.model

import android.content.Context
import com.neeplayer.*
import com.neeplayer.Preferences.Item.LongItem.*
import com.neeplayer.Preferences.Item.StringItem.*
import org.jetbrains.anko.toast
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

class NowPlayingModel(val context: Context, val lastFm: LastFmService, val preferences: Preferences, val database: Database) {

    private val nowPlayingSubject = BehaviorSubject.create<Playlist>()

    var nowPlaying: Playlist? = database.restorePlaylist(preferences.get(NOW_PLAYING_SONG_ID))
        set(value) {
            field = value
            if (value != null) {
                nowPlayingSubject.onNext(value)
            }
        }

    val nowPlayingObservable: Observable<Playlist>
        get() = nowPlayingSubject

    var progress = 0
    set(value) {
        field = value
        progressListeners.forEach { it() }
    }

    private val progressListeners = mutableSetOf<() -> Unit>()

    fun addProgressListener(listener: () -> Unit) {
        listener()
        progressListeners.add(listener)
    }


    fun removeProgressListener(listener: () -> Unit) {
        progressListeners.remove(listener)
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