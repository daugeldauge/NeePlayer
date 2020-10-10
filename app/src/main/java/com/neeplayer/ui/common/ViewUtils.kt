package com.neeplayer.ui.common

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.abs

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
    this.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (onSlide == null || slideOffset == 0f) {
                return
            }

            onSlide(if (slideOffset > 0) {
                1 - slideOffset
            } else {
                abs(slideOffset)
            })
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

val Fragment.actionBar: ActionBar?
    get() = (activity as AppCompatActivity).supportActionBar

fun View.onFirstLayout(block: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            block()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}