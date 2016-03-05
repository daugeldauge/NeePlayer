package com.neeplayer

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SlidingPanelBehavior<V : View> : BottomSheetBehavior<V> {

    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN  && !parent.isPointInChildBounds(child, event.x.toInt(), event.y.toInt())) {
            return false;
        }

        return super.onTouchEvent(parent, child, event)
    }
}