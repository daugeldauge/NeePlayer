package com.neeplayer

import android.app.Application
import com.neeplayer.di.AppComponent
import com.neeplayer.di.AppModule
import com.neeplayer.di.DaggerAppComponent

class NeePlayerApp : Application() {
    companion object {
        lateinit var component: AppComponent
        private set
    }

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().appModule(AppModule(this)).build();
    }

}
