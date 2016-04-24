package com.neeplayer

import android.content.Context
import com.neeplayer.model.Database
import com.neeplayer.model.NowPlayingModel
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Provides
    @Singleton
    fun providePreferences(): Preferences = Preferences(context)

    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun provideNowPlayingModel(lastFmService: LastFmService, preferences: Preferences, database: Database) = NowPlayingModel(context, lastFmService, preferences, database)

    @Provides
    @Singleton
    fun provideStorIoContentResolver(): StorIOContentResolver = DefaultStorIOContentResolver.builder()
                    .contentResolver(context.contentResolver)
                    .build()
}