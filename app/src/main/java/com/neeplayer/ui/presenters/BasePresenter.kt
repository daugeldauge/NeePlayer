package com.neeplayer.ui.presenters

import android.os.Bundle

abstract class BasePresenter<V> {

    protected var view: V? = null

    open fun bind(view: V) {
        this.view = view
    }

    open fun onRestoreInstanceState(savedInstanceState: Bundle?) {}

    open fun onSaveInstanceState(outState: Bundle) {}

    open fun unbind() {
        view = null
    }
}