package com.neeplayer

import android.app.Application
import com.neeplayer.di.AppComponent
import com.neeplayer.di.AppModule
import com.neeplayer.di.DaggerAppComponent
import com.neeplayer.di.Scrobbler
import javax.inject.Inject

class NeePlayerApp : Application() {
    companion object {
        lateinit var component: AppComponent
        private set
    }

    @Inject
    @Suppress("unused")
    internal lateinit var scrobbler: Scrobbler

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().appModule(AppModule(this)).build();
        component.inject(this)
    }

}
