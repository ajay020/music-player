package com.example.musicplayer.di

import com.example.musicplayer.data.database.AppDatabase
import com.example.musicplayer.data.database.PlaylistDao

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): AppDatabase {
        return AppDatabase.getDatabase(context, scope)
    }

    @Provides
    fun providePlaylistDao(appDatabase: AppDatabase): PlaylistDao {
        return appDatabase.playlistDao()
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }
}