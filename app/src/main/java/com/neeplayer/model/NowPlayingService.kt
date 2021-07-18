package com.neeplayer.model

import com.neeplayer.model.Preferences.Item.LongItem.NowPlayingSongId
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NowPlayingService(
    private val preferences: Preferences,
    private val database: Database,
) {

    private val nowPlayingState = MutableStateFlow<Playlist?>(null)
    private val nowPlaying: Playlist?
        get() = nowPlayingState.value

    data class Progress(val payload: Any, val value: Int)

    private val progressState = MutableStateFlow(Progress(Unit, 0))


    val nowPlayingChanges: Flow<Playlist> = nowPlayingState.filterNotNull()
    val progressChanges: Flow<Progress> = progressState

    fun alter(block: (current: Playlist?) -> Playlist?) {
        nowPlayingState.update(block)
    }

    fun offerProgress(payload: Any, value: Int) {
        progressState.value = Progress(payload, value)
    }

    @OptIn(DelicateCoroutinesApi::class)
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
