package com.neeplayer.ui.presenters

import android.os.Bundle

abstract class BasePresenter<V>(protected val view: V) {

    open fun onRestoreInstanceState(savedInstanceState: Bundle?) {}

    open fun onSaveInstanceState(outState: Bundle) {}

    open fun onDestroy() {}
}