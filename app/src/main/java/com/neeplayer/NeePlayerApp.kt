package com.neeplayer

import android.app.Application

class NeePlayerApp : Application() {
    companion object {
        var component: AppComponent? = null
    }

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().appModule(AppModule(this)).build();
    }

}
