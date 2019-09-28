package com.neeplayer.model

import android.content.Context
import com.neeplayer.R
import com.neeplayer.minutes
import com.neeplayer.model.Preferences.Item.BooleanItem.ScrobblingEnabled
import com.neeplayer.seconds
import org.jetbrains.anko.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scrobbler @Inject constructor(
        private val context: Context,
        private val service: NowPlayingService,
        private val lastFm: LastFmService,
        private val preferences: Preferences
) {

    private val MIN_SONG_LENGTH_TO_SCROBBLE = 30.seconds
    private val SCROBBLE_FRACTION_THRESHOLD = 0.5
    private val SCROBBLE_THRESHOLD = 4.minutes

    private var isCurrentSongScrobbled = false
    private var playingPeriodStart: Long? = null
    private var overallPlaying = 0L

    private var lastSong: Song? = null

    init {
        service.nowPlayingObservable.subscribe {
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

        service.progressObservable.subscribe {
                val song = lastSong ?: return@subscribe
                val currentOverall = currentOverallPlaying() ?: return@subscribe

                if (preferences.isSignedIn() &&
                        preferences.getOrDefault(ScrobblingEnabled) &&
                        !isCurrentSongScrobbled &&
                        song.duration > MIN_SONG_LENGTH_TO_SCROBBLE && (currentOverall >= song.duration * SCROBBLE_FRACTION_THRESHOLD || currentOverall >= SCROBBLE_THRESHOLD )
                ) {
                    scrobble(song)
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

        lastFm.scrobble(
                track = song.title ?: return,
                album = song.album.title,
                artist = song.album.artist.name,
                timestamp = System.currentTimeMillis() / 1000,
                sessionKey = preferences.get(Preferences.Item.StringItem.SessionKey) ?: return
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