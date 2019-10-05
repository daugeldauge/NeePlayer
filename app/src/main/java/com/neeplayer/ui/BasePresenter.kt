package com.neeplayer.ui

import androidx.annotation.CallSuper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BasePresenter<V: Any> {

    protected lateinit var view: V

    protected val mainScope = MainScope()

    @CallSuper
    open fun bind(view: V) {
        this.view = view
    }

    @CallSuper
    open fun unbind() {
        mainScope.cancel()
    }
}