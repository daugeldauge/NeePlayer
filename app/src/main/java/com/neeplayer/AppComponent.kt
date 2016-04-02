package com.neeplayer

import com.neeplayer.ui.fragments.MainFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(LastFmModule::class, AppModule::class))
@Singleton
interface AppComponent {
    fun inject(fragment: MainFragment)
}