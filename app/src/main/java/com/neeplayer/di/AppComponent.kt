package com.neeplayer.di

import com.neeplayer.NeePlayerApp
import com.neeplayer.ui.albums.AlbumsFragment
import com.neeplayer.ui.now_playing.MusicService
import com.neeplayer.ui.now_playing.NowPlayingFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(LastFmModule::class, AppModule::class))
@Singleton
interface AppComponent {
    fun plus(activityModule: ActivityModule): ActivityComponent

    fun inject(neePlayerApp: NeePlayerApp)
    fun inject(albumsFragment: AlbumsFragment)
    fun inject(nowPlayingFragment: NowPlayingFragment)
    fun inject(musicService: MusicService)
}