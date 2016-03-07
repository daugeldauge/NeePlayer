package com.neeplayer

import android.app.Application
import com.neeplayer.model.Model

class NeePlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Model.init(this)
    }

}