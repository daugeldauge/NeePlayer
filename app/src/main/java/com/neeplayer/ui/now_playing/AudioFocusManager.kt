package com.neeplayer.ui.now_playing

import android.app.Application
import android.media.AudioManager
import com.neeplayer.R
import com.neeplayer.toast

class AudioFocusManager(
    private val context: Application,
    private val audioManager: AudioManager,
    private val volumeModifier: (Float) -> Unit,
    private val focusLossListener: () -> Unit,
    private val focusTransientLossListener: () -> Unit,
    private val focusGainFromTransientLossListener: () -> Unit
) {

    private var lastKnownAudioFocusState: Int? = null

    fun requestFocus() {
        @Suppress("DEPRECATION") // alternative is not available for minSdk 23
        when (audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> Unit // ignore
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                context.toast(R.string.audio_focus_request_error)
            }
        }
    }

    fun abandonFocus() {
        @Suppress("DEPRECATION") // alternative is not available for minSdk 23
        audioManager.abandonAudioFocus(audioFocusListener)
    }

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                when (lastKnownAudioFocusState) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> focusGainFromTransientLossListener()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> volumeModifier(1f)
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> focusLossListener()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> focusTransientLossListener()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> volumeModifier(0.2f)
        }
        lastKnownAudioFocusState = it
    }
}