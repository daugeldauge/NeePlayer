package com.neeplayer.model

import com.neeplayer.RxProperty
import com.neeplayer.filterNotNull
import com.neeplayer.model.Preferences.Item.LongItem.NOW_PLAYING_SONG_ID
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NowPlayingService
@Inject constructor(
        private val preferences: Preferences,
        private val database: Database
) {

    private val nowPlayingSubject = BehaviorSubject.create<Playlist?>()

    var nowPlaying by RxProperty(null, nowPlayingSubject)

    val nowPlayingObservable: Observable<Playlist>
        get() = nowPlayingSubject.filterNotNull()

    private val progressSubject = BehaviorSubject.create<Int>()

    val progressObservable: Observable<Int>
        get() = progressSubject

    var progress by RxProperty(0, progressSubject)

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
}