package com.neeplayer.model

import android.content.Context
import com.neeplayer.*
import org.jetbrains.anko.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

object Model {
    private val SHARED_PREFERENCES_NAME = "MAIN"
    private val NOW_PLAYING_SONG = "NOW_PLAYING_SONG"

    private lateinit var context: Context

    private val prefs by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    @Inject
    lateinit internal var lastFm: LastFmService

    @Inject
    lateinit internal var preferences: Preferences

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

        //TODO inject
        lastFm = LastFmModule().provideLastFmService()
        preferences = AppModule(context).providePreferences()
    }

    fun save() {
        val nowPlaying = nowPlaying ?: return
        prefs.edit().putLong(NOW_PLAYING_SONG, nowPlaying.currentSong.id).apply()
    }

    fun scrobble() {
        val song = (nowPlaying?.currentSong) ?: return

        lastFm.scrobble(
                track = song.title ?: return,
                album = song.album.title,
                artist = song.album.artist.name,
                timestamp = System.currentTimeMillis() / 1000,
                sessionKey = preferences.get(Preferences.Item.SESSION_KEY) ?: return
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