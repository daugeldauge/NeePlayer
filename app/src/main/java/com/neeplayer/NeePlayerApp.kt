package com.neeplayer

import android.app.Application

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
