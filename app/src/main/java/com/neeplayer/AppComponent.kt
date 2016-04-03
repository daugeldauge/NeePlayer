package com.neeplayer

import com.neeplayer.ui.fragments.ArtistFragment
import com.neeplayer.ui.fragments.MainFragment
import com.neeplayer.ui.presenters.NowPlayingPresenter
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(LastFmModule::class, AppModule::class))
@Singleton
interface AppComponent {
    fun inject(fragment: MainFragment)
    fun inject(artistFragment: ArtistFragment)
    fun inject(nowPlayingPresenter: NowPlayingPresenter)
}