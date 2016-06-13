package com.neeplayer.ui

import android.os.Bundle
import android.support.annotation.CallSuper
import icepick.Icepick
import rx.subscriptions.CompositeSubscription

abstract class BasePresenter<V: Any> {

    lateinit protected var view: V

    protected val subscriptions = CompositeSubscription()

    @CallSuper
    open fun bind(view: V) {
        this.view = view
    }

    @CallSuper
    open fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        Icepick.restoreInstanceState(this, savedInstanceState)
    }

    @CallSuper
    open fun onSaveInstanceState(outState: Bundle) {
        Icepick.saveInstanceState(this, outState)
    }

    @CallSuper
    open fun unbind() {
        subscriptions.clear()
    }
}