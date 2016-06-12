package com.neeplayer.ui

import android.os.Bundle
import icepick.Icepick
import rx.subscriptions.CompositeSubscription

abstract class BasePresenter<V: Any> {

    lateinit protected var view: V

    protected val subscriptions = CompositeSubscription()

    open fun bind(view: V) {
        this.view = view
    }

    open fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        Icepick.restoreInstanceState(this, savedInstanceState)
    }

    open fun onSaveInstanceState(outState: Bundle) {
        Icepick.saveInstanceState(this, outState)
    }

    open fun unbind() {
        subscriptions.clear()
    }
}