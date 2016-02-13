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
        set(value) {
            field = value
            applyState()
        }

    private val collapsedView : View
        get() = getChildAt(1)

    private val expandedView : View
        get() = getChildAt(0)

    var isFirstMeasure = true

    override fun onFinishInflate() {
        super.onFinishInflate()

        onClick {
            expanded = !expanded
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (isFirstMeasure) {
            applyState(animated = false)
        }
    }

    private fun applyState(animated: Boolean = true) {

        val targetTranslation =
                if (expanded) 0f
                else (measuredHeight - collapsedView.measuredHeight).toFloat()

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