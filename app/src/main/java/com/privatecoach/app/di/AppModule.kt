package com.privatecoach.app.di

import com.privatecoach.app.data.datastore.SettingsDataStore
import com.privatecoach.app.data.repository.SettingsRepositoryImpl
import com.privatecoach.app.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl
}
