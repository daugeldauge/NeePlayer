package com.neeplayer.model

import com.neeplayer.model.Preferences.Item.LongItem.NowPlayingSongId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NowPlayingService @Inject constructor(
        private val preferences: Preferences,
        private val database: Database
) {

    private val nowPlayingChannel = ConflatedBroadcastChannel<Playlist?>()
    private val nowPlaying: Playlist?
        get() = nowPlayingChannel.valueOrNull


    private val progressChannel = ConflatedBroadcastChannel<Int>()


    val nowPlayingFlow = nowPlayingChannel.asFlow().filterNotNull()
    val progressFlow = progressChannel.asFlow()

    fun alter(block: (current: Playlist?) -> Playlist?) {
        nowPlayingChannel.offer(nowPlaying.let(block))
    }

    fun offerProgress(value: Int) {
        progressChannel.offer(value)
    }

    fun tryRestoreNowPlaying() {
        if (nowPlaying != null) {
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val restoredPlaylist = database.restorePlaylist(preferences.get(NowPlayingSongId))
            alter { restoredPlaylist }
        }
    }

    fun save() {
        val nowPlaying = nowPlaying ?: return
        preferences.put(NowPlayingSongId, nowPlaying.currentSong.id)
    }
}