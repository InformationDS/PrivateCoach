package com.privatecoach.app.di

import com.privatecoach.app.core.query.QueryEngine
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QueryModule {
    @Provides
    @Singleton
    fun provideQueryEngine(workoutRepository: WorkoutRepository): QueryEngine {
        return QueryEngine(workoutRepository)
    }
}
