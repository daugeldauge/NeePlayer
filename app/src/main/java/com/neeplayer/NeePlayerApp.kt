package com.neeplayer

import android.app.Application
import com.neeplayer.di.activityModule
import com.neeplayer.di.appModule
import com.neeplayer.di.networkModule
import com.neeplayer.model.Scrobbler
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

@Suppress("unused")
class NeePlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initLogger()
        createKoin()
        startScrobbling()
    }

    private fun createKoin() {
        startKoin {
            androidContext(this@NeePlayerApp)

            modules(listOf(
                    appModule,
                    activityModule,
                    networkModule
            ))
        }
    }

    private fun startScrobbling() {
        get<Scrobbler>()
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
