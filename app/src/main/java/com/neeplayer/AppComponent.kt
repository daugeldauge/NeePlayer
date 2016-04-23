package com.neeplayer

import com.neeplayer.ui.activities.MainActivity
import com.neeplayer.ui.fragments.AlbumsFragment
import com.neeplayer.ui.fragments.ArtistsFragment
import com.neeplayer.ui.presenters.NowPlayingPresenter
import com.neeplayer.ui.views.impl.MusicService
import com.neeplayer.ui.views.impl.NowPlayingFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(LastFmModule::class, AppModule::class))
@Singleton
interface AppComponent {
    fun inject(fragment: ArtistsFragment)
    fun inject(artistFragment: AlbumsFragment)
    fun inject(nowPlayingFragment: NowPlayingFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(musicService: MusicService)
}