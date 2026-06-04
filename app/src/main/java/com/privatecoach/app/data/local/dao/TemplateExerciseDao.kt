package com.privatecoach.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.privatecoach.app.data.local.entity.TemplateExerciseEntity

@Dao
interface TemplateExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<TemplateExerciseEntity>)

    @Query("DELETE FROM template_exercises WHERE template_id = :templateId")
    suspend fun deleteByTemplateId(templateId: Long)
}
