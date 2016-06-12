package com.neeplayer.di

import com.neeplayer.ui.albums.AlbumsFragment
import com.neeplayer.ui.artists.ArtistsFragment
import com.neeplayer.ui.now_playing.MusicService
import com.neeplayer.ui.now_playing.NowPlayingFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(LastFmModule::class, AppModule::class))
@Singleton
interface AppComponent {
    fun plus(activityModule: ActivityModule): ActivityComponent

    fun inject(fragment: ArtistsFragment)
    fun inject(artistFragment: AlbumsFragment)
    fun inject(nowPlayingFragment: NowPlayingFragment)
    fun inject(musicService: MusicService)
}