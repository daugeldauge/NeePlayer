package com.neeplayer.di

import android.content.Context
import com.neeplayer.model.Database
import com.neeplayer.model.LastFmService
import com.neeplayer.model.NowPlayingModel
import com.neeplayer.model.Preferences
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun provideStorIoContentResolver(): StorIOContentResolver = DefaultStorIOContentResolver.builder()
                    .contentResolver(context.contentResolver)
                    .build()
}