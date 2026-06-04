package com.privatecoach.app.di

import com.privatecoach.app.core.ai.AiApiService
import com.privatecoach.app.core.ai.GeminiApiServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds @Singleton
    abstract fun bindAiApiService(impl: GeminiApiServiceImpl): AiApiService
}
