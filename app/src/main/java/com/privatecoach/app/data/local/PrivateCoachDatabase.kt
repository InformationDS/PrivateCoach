package com.privatecoach.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.privatecoach.app.data.local.converter.Converters
import com.privatecoach.app.data.local.dao.CardioDetailDao
import com.privatecoach.app.data.local.dao.ExerciseDao
import com.privatecoach.app.data.local.dao.TemplateExerciseDao
import com.privatecoach.app.data.local.dao.TrainingTemplateDao
import com.privatecoach.app.data.local.dao.WorkoutDao
import com.privatecoach.app.data.local.entity.CardioDetailEntity
import com.privatecoach.app.data.local.entity.ExerciseEntity
import com.privatecoach.app.data.local.entity.TemplateExerciseEntity
import com.privatecoach.app.data.local.entity.TrainingTemplateEntity
import com.privatecoach.app.data.local.entity.WorkoutEntity

@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        CardioDetailEntity::class,
        TrainingTemplateEntity::class,
        TemplateExerciseEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PrivateCoachDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun cardioDetailDao(): CardioDetailDao
    abstract fun trainingTemplateDao(): TrainingTemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao
}
