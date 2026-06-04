package com.privatecoach.app.data.mapper

import com.privatecoach.app.core.model.TemplateExercise
import com.privatecoach.app.core.model.TrainingTemplate
import com.privatecoach.app.data.local.entity.TemplateExerciseEntity
import com.privatecoach.app.data.local.entity.TrainingTemplateEntity
import com.privatecoach.app.data.local.relation.TemplateWithExercises

fun TemplateWithExercises.toDomain(): TrainingTemplate = TrainingTemplate(
    id = template.id,
    name = template.name,
    type = template.type,
    bodyPart = template.bodyPart,
    sortOrder = template.sortOrder,
    createdAt = template.createdAt,
    exercises = exercises.map { it.toDomain() }
)

fun TemplateExerciseEntity.toDomain(): TemplateExercise = TemplateExercise(
    id = id,
    templateId = templateId,
    name = name,
    weightUnit = weightUnit,
    sortOrder = sortOrder,
    notes = notes
)

fun TrainingTemplate.toTemplateEntity(): TrainingTemplateEntity = TrainingTemplateEntity(
    id = id,
    name = name,
    type = type,
    bodyPart = bodyPart,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun TemplateExercise.toEntity(templateId: Long): TemplateExerciseEntity = TemplateExerciseEntity(
    id = id,
    templateId = templateId,
    name = name,
    weightUnit = weightUnit,
    sortOrder = sortOrder,
    notes = notes
)
