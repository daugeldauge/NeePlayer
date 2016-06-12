package com.neeplayer

import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

open class RxSchedulers @Inject constructor() {
    fun io() = Schedulers.io()
    fun computation() = Schedulers.computation()
    fun mainThread() = AndroidSchedulers.mainThread()
}