package com.privatecoach.app.di

import android.content.Context
import androidx.room.Room
import com.privatecoach.app.data.local.PrivateCoachDatabase
import com.privatecoach.app.data.local.dao.CardioDetailDao
import com.privatecoach.app.data.local.dao.ExerciseDao
import com.privatecoach.app.data.local.dao.TemplateExerciseDao
import com.privatecoach.app.data.local.dao.TrainingTemplateDao
import com.privatecoach.app.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PrivateCoachDatabase =
        Room.databaseBuilder(context, PrivateCoachDatabase::class.java, "privatecoach.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideWorkoutDao(db: PrivateCoachDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideExerciseDao(db: PrivateCoachDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideCardioDetailDao(db: PrivateCoachDatabase): CardioDetailDao = db.cardioDetailDao()
    @Provides fun provideTrainingTemplateDao(db: PrivateCoachDatabase): TrainingTemplateDao = db.trainingTemplateDao()
    @Provides fun provideTemplateExerciseDao(db: PrivateCoachDatabase): TemplateExerciseDao = db.templateExerciseDao()
}
