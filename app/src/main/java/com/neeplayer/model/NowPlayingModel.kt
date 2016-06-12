package com.neeplayer.model

import android.content.Context
import com.neeplayer.*
import com.neeplayer.model.Preferences.Item.LongItem.*
import com.neeplayer.model.Preferences.Item.StringItem.*
import com.neeplayer.model.Preferences.Item.BooleanItem.*
import org.jetbrains.anko.toast
import rx.Observable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

class NowPlayingModel(
        private val context: Context,
        private val lastFm: LastFmService,
        private val preferences: Preferences,
        private val database: Database
) {

    private val nowPlayingSubject = BehaviorSubject.create<Playlist>()

    var nowPlaying: Playlist? = null
        set(value) {
            field = value
            if (value != null) {
                nowPlayingSubject.onNext(value)
            }
        }

    val nowPlayingObservable: Observable<Playlist>
        get() = nowPlayingSubject

    private val progressSubject = BehaviorSubject.create<Int>()

    val progressObservable: Observable<Int>
        get() = progressSubject

    var progress = 0
    set(value) {
        field = value
        progressSubject.onNext(value)
    }

    fun tryRestoreNowPlaying() {
        if (nowPlaying != null) {
            return
        }

        Single.create<Playlist?> { it.onSuccess(database.restorePlaylist(preferences.get(NOW_PLAYING_SONG_ID))) }
            .subscribeOn(Schedulers.io())
            .subscribe { nowPlaying = it }
    }

    fun save() {
        val nowPlaying = nowPlaying ?: return
        preferences.put(NOW_PLAYING_SONG_ID, nowPlaying.currentSong.id)
    }

    fun scrobble(song: Song) {
        if (preferences.getOrDefault(SCROBBLING) == false) {
            return
        }

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