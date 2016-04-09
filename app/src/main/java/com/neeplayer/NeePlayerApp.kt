package com.neeplayer

import android.app.Application

class NeePlayerApp : Application() {
    companion object {
        lateinit var component: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().appModule(AppModule(this)).build();
    }

}
