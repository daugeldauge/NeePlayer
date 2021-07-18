package com.neeplayer.model

import com.neeplayer.model.Preferences.Item.LongItem.NowPlayingSongId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class NowPlayingService(
        private val preferences: Preferences,
        private val database: Database
) {

    private val nowPlayingChannel = ConflatedBroadcastChannel<Playlist?>()
    private val nowPlaying: Playlist?
        get() = nowPlayingChannel.valueOrNull

    data class Progress(val payload: Any, val value: Int)

    private val progressChannel = ConflatedBroadcastChannel<Progress>()


    val nowPlayingFlow = nowPlayingChannel.asFlow().filterNotNull()
    val progressFlow = progressChannel.asFlow()

    fun alter(block: (current: Playlist?) -> Playlist?) {
        nowPlayingChannel.offer(nowPlaying.let(block))
    }

    fun offerProgress(payload: Any, value: Int) {
        progressChannel.offer(Progress(payload, value))
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
