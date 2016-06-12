package com.neeplayer.di

import com.neeplayer.ui.MainActivity
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(ActivityModule::class))
@ActivityScope
interface ActivityComponent {
    fun inject(mainActivity: MainActivity)
}