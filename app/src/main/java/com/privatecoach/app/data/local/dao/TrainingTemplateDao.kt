package com.privatecoach.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.privatecoach.app.data.local.entity.TrainingTemplateEntity
import com.privatecoach.app.data.local.relation.TemplateWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingTemplateDao {
    @Transaction
    @Query("SELECT * FROM training_templates ORDER BY sort_order ASC, created_at DESC")
    fun getAllTemplates(): Flow<List<TemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM training_templates ORDER BY sort_order ASC, created_at DESC")
    suspend fun getAllTemplatesOnce(): List<TemplateWithExercises>

    @Transaction
    @Query("SELECT * FROM training_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Long): TemplateWithExercises?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TrainingTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<TrainingTemplateEntity>): List<Long>

    @Update
    suspend fun updateTemplate(template: TrainingTemplateEntity)

    @Query("DELETE FROM training_templates WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Long)
}
