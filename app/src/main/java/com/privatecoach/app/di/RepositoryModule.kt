package com.privatecoach.app.di

import com.privatecoach.app.data.repository.TemplateRepositoryImpl
import com.privatecoach.app.data.repository.WorkoutRepositoryImpl
import com.privatecoach.app.domain.repository.TemplateRepository
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds @Singleton
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository
}
