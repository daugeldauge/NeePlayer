package com.neeplayer

import android.app.Application
import com.neeplayer.di.AppModule
import com.neeplayer.di.DaggerAppComponent
import com.neeplayer.model.Scrobbler
import timber.log.Timber
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
        initLogger()
        appComponent.inject(this)
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
