package com.neeplayer.ui.now_playing

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import com.neeplayer.R
import com.neeplayer.toast

class MusicPlayer(private val context: Application) {

    private enum class State {
        IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED
    }

    private val player = MediaPlayer().apply {
        setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)

        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        setOnPreparedListener {
            state = State.PREPARED
            if (playWhenReady) {
                play()
            }
        }

        setOnErrorListener { _, _, _ ->
            this@MusicPlayer.reset()
            context.toast(R.string.media_player_error)
            true
        }
    }

    private var state = State.IDLE

    private var uri: Uri? = null
    private var playWhenReady: Boolean = false

    val currentPosition: Int
        get() = player.currentPosition

    fun setCompletionListener(completionListener: () -> Unit) {
        player.setOnCompletionListener {
            reset()
            completionListener()
        }
    }

    fun setState(uri: Uri, playWhenReady: Boolean) {

        if (this.uri != uri) {
            this.uri = uri
            prepareUri(uri)
        }

        if (this.playWhenReady != playWhenReady) {
            this.playWhenReady = playWhenReady

            if (playWhenReady) {
                play()
            } else {
                pause()
            }
        }
    }

    fun seek(progress: Int) {
        if (state == State.STARTED || state == State.PAUSED) {
            player.seekTo(progress)
        }
    }

    fun setVolume(volume: Float) {
        player.setVolume(volume, volume)
    }

    fun release() {
        player.stop()
        player.release()
    }

    private fun prepareUri(uri: Uri) {
        try {
            reset()
            setDataSource(uri)
            prepareAsync()
        } catch (e: Exception) {
            context.toast("Error on setting data source")
        }
    }

    private fun pause() {
        if (state == State.STARTED) {
            player.pause()
            state = State.PAUSED
        }
    }

    private fun play() {
        if (state == State.PREPARED || state == State.PAUSED) {
            player.start()
            state = State.STARTED
        }
    }

    private fun prepareAsync() {
        player.prepareAsync()
        state = State.PREPARING
    }

    private fun setDataSource(uri: Uri) {
        player.setDataSource(context, uri)
        state = State.INITIALIZED
    }

    private fun reset() {
        player.reset()
        state = State.IDLE
    }

}