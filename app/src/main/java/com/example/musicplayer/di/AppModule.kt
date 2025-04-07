package com.example.musicplayer.di

import android.app.Application
import android.content.Context
import com.example.musicplayer.data.repository.AlbumRepository
import com.example.musicplayer.data.repository.SongRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSongRepository(@ApplicationContext context: Context): SongRepository {
        return SongRepository(context)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }


    @Provides
    @Singleton
    fun provideAlbumRepository(@ApplicationContext context: Context): AlbumRepository {
        return AlbumRepository(context)
    }
}
