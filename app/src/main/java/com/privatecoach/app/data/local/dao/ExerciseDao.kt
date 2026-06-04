package com.privatecoach.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.privatecoach.app.data.local.entity.ExerciseEntity

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteById(exerciseId: Long)

    @Query("DELETE FROM exercises WHERE workout_id = :workoutId")
    suspend fun deleteByWorkoutId(workoutId: Long)
}
