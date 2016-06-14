package com.neeplayer

import android.app.Application
import com.neeplayer.di.AppModule
import com.neeplayer.di.DaggerAppComponent
import com.neeplayer.model.Scrobbler
import javax.inject.Inject

class NeePlayerApp : Application() {
    val appComponent by lazy {
        DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    @Inject
    @Suppress("unused")
    internal lateinit var scrobbler: Scrobbler

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
    }

}
