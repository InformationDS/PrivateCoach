package com.privatecoach.app.domain.repository

import com.privatecoach.app.core.model.TrainingTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<TrainingTemplate>>
    suspend fun getAllTemplatesOnce(): List<TrainingTemplate>
    suspend fun getTemplateById(id: Long): TrainingTemplate?
    suspend fun saveTemplate(template: TrainingTemplate): Long
    suspend fun updateTemplate(template: TrainingTemplate)
    suspend fun deleteTemplate(id: Long)
    suspend fun insertTemplates(templates: List<TrainingTemplate>)
}
