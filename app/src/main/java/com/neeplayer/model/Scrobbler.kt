package com.neeplayer.model

import android.app.Application
import com.neeplayer.R
import com.neeplayer.minutes
import com.neeplayer.model.Preferences.Item.BooleanItem.ScrobblingEnabled
import com.neeplayer.network.Response
import com.neeplayer.network.lastfm.LastFmApi
import com.neeplayer.seconds
import com.neeplayer.toast
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class Scrobbler(
        private val context: Application,
        service: NowPlayingService,
        private val lastFm: Lazy<LastFmApi>,
        private val preferences: Preferences
) {

    private val MIN_SONG_LENGTH_TO_SCROBBLE = 30.seconds
    private val SCROBBLE_FRACTION_THRESHOLD = 0.5
    private val SCROBBLE_THRESHOLD = 4.minutes

    private var isCurrentSongScrobbled = false
    private var playingPeriodStart: Long? = null
    private var overallPlaying = 0L

    private var lastSong: Song? = null

    private val mainScope = MainScope()

    init {
        mainScope.launch {
            service.nowPlayingFlow.collect {
                if (lastSong != it.currentSong) {
                    overallPlaying = 0
                    playingPeriodStart = null
                    isCurrentSongScrobbled = false
                }

                if (it.paused) {
                    overallPlaying = currentOverallPlaying() ?: overallPlaying
                    playingPeriodStart = null
                } else if (playingPeriodStart == null) {
                    playingPeriodStart = time()
                }

                lastSong = it.currentSong
            }
        }

        mainScope.launch {
            service.progressFlow.collect {
                val song = lastSong ?: return@collect
                val currentOverall = currentOverallPlaying() ?: return@collect

                if (preferences.isSignedIn() &&
                        preferences.getOrDefault(ScrobblingEnabled) &&
                        !isCurrentSongScrobbled &&
                        song.duration > MIN_SONG_LENGTH_TO_SCROBBLE && (currentOverall >= song.duration * SCROBBLE_FRACTION_THRESHOLD || currentOverall >= SCROBBLE_THRESHOLD)
                ) {
                    scrobble(song)
                }
            }
        }
    }

    private fun time() = System.currentTimeMillis()

    private fun currentOverallPlaying(): Long? {
        val periodStart = playingPeriodStart ?: return null
        return overallPlaying + time() - periodStart
    }

    private fun scrobble(song: Song) {

        isCurrentSongScrobbled = true
        val songTitle = song.title ?: return
        val sessionKey = preferences.get(Preferences.Item.StringItem.SessionKey) ?: return

        mainScope.launch {
            val response = lastFm.value.scrobble(
                    track = songTitle,
                    album = song.album.title,
                    artist = song.album.artist.name,
                    timestamp = System.currentTimeMillis() / 1000,
                    sessionKey = sessionKey
            )

            when (response) {
                is Response.Success<*> -> context.toast(context.getString(R.string.scrobble_success, song.title))
                is Response.Error -> context.toast(R.string.scrobble_error)
            }
        }

    }
}