package com.neeplayer

import android.app.Application
import android.content.Context
import com.neeplayer.model.NowPlayingModel

class NeePlayerApp : Application() {
    companion object {
        var component: AppComponent? = null
    }

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().appModule(AppModule(this)).build();
    }

}
