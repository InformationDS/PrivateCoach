package com.privatecoach.app.data.repository

import com.privatecoach.app.core.model.TrainingTemplate
import com.privatecoach.app.data.local.dao.TemplateExerciseDao
import com.privatecoach.app.data.local.dao.TrainingTemplateDao
import com.privatecoach.app.data.mapper.toDomain
import com.privatecoach.app.data.mapper.toEntity
import com.privatecoach.app.data.mapper.toTemplateEntity
import com.privatecoach.app.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TrainingTemplateDao,
    private val templateExerciseDao: TemplateExerciseDao
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<TrainingTemplate>> =
        templateDao.getAllTemplates().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllTemplatesOnce(): List<TrainingTemplate> =
        templateDao.getAllTemplatesOnce().map { it.toDomain() }

    override suspend fun insertTemplates(templates: List<TrainingTemplate>) {
        templates.forEach { saveTemplate(it) }
    }

    override suspend fun getTemplateById(id: Long): TrainingTemplate? =
        templateDao.getTemplateById(id)?.toDomain()

    override suspend fun saveTemplate(template: TrainingTemplate): Long {
        val entity = template.toTemplateEntity()
        val templateId = templateDao.insertTemplate(entity)
        templateExerciseDao.insertAll(
            template.exercises.mapIndexed { index, ex ->
                ex.toEntity(templateId).copy(sortOrder = index)
            }
        )
        return templateId
    }

    override suspend fun updateTemplate(template: TrainingTemplate) {
        templateDao.updateTemplate(template.toTemplateEntity())
        templateExerciseDao.deleteByTemplateId(template.id)
        templateExerciseDao.insertAll(
            template.exercises.mapIndexed { index, ex ->
                ex.toEntity(template.id).copy(sortOrder = index)
            }
        )
    }

    override suspend fun deleteTemplate(id: Long) {
        templateDao.deleteTemplate(id)
    }
}
