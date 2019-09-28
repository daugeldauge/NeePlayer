package com.neeplayer.ui

import android.support.annotation.CallSuper
import rx.subscriptions.CompositeSubscription

abstract class BasePresenter<V: Any> {

    protected lateinit var view: V

    protected val subscriptions = CompositeSubscription()

    @CallSuper
    open fun bind(view: V) {
        this.view = view
    }

    @CallSuper
    open fun unbind() {
        subscriptions.clear()
    }
}