package com.neeplayer.di

import androidx.appcompat.app.AppCompatActivity
import com.neeplayer.ui.Router
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: AppCompatActivity) {

    @Provides
    @ActivityScope
    fun provideRouter() = Router(activity)
}
