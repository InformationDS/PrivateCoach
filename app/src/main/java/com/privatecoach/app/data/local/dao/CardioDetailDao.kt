package com.privatecoach.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.privatecoach.app.data.local.entity.CardioDetailEntity

@Dao
interface CardioDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: CardioDetailEntity): Long

    @Update
    suspend fun update(detail: CardioDetailEntity)

    @Query("SELECT * FROM cardio_details WHERE exercise_id = :exerciseId")
    suspend fun getByExerciseId(exerciseId: Long): CardioDetailEntity?
}
