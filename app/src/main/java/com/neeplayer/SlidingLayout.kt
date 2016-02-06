package com.neeplayer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import org.jetbrains.anko.onClick

class SlidingLayout(context: Context?, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    var expanded = false;

    private val collapsedView : View
        get() = getChildAt(1)

    private val expandedView : View
        get() = getChildAt(0)


    override fun onFinishInflate() {
        super.onFinishInflate()

        onClick {
            expanded = !expanded
            applyState()
        }
    }


    private fun applyState(animated: Boolean = true) {

        val targetTranslation =
                if (expanded) 0f
                else (height - collapsedView.height).toFloat()

        val targetCollapsedViewAlpha = if (expanded) 0f else 1f


        if (animated) {
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(collapsedView, ALPHA, targetCollapsedViewAlpha),
                    ObjectAnimator.ofFloat(this, TRANSLATION_Y, targetTranslation))

            animatorSet.start()
        } else {
            collapsedView.alpha = targetCollapsedViewAlpha
            translationY = targetTranslation
        }
    }

}