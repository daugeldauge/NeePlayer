package com.neeplayer.ui

import android.os.Handler
import android.os.Looper
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.widget.SeekBar

fun SeekBar.onUserSeek(
        onProgress: ((progress: Int) -> Unit)? = null,
        onTouchStopped: (() -> Unit)? = null
) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser && onProgress != null) {
                onProgress(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            onTouchStopped?.invoke()
        }
    })
}

fun BottomSheetBehavior<out View>.setCallback(
        onSlide: ((offset: Float) -> Unit)? = null,
        onStateChanged: ((state: Int) -> Unit)? = null
) {
    this.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (onSlide == null || slideOffset == 0f) {
                return
            }

            onSlide(if (slideOffset > 0) { 1 - slideOffset } else { Math.abs(slideOffset) })
        }

        override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State state: Int) {
            onStateChanged?.invoke(state)
        }
    })
}

private val uiThreadHandler = Handler(Looper.getMainLooper())

private fun Fragment.isAlive() = activity != null && isAdded && !isDetached && view != null && !isRemoving

fun Fragment.uiThread(action: () -> Unit) {
    if (Looper.myLooper() == uiThreadHandler.looper && isAlive()) {
        action.invoke()
    } else {
        uiThreadHandler.post {
            if (isAlive()) {
                action.invoke()
            }
        }
    }
}