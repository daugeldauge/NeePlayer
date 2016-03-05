package com.neeplayer

import android.support.design.widget.BottomSheetBehavior
import android.view.View
import android.widget.SeekBar

fun SeekBar.onUserSeek(action: (progress: Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                action(progress)
            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}

        override fun onStopTrackingTouch(p0: SeekBar?) {}
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