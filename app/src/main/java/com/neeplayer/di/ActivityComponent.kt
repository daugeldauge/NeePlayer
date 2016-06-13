package com.neeplayer.di

import com.neeplayer.ui.MainActivity
import com.neeplayer.ui.artists.ArtistsFragment
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(ActivityModule::class))
@ActivityScope
interface ActivityComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(artistsFragment: ArtistsFragment)
}