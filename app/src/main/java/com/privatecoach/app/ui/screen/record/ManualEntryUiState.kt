package com.privatecoach.app.ui.screen.record

import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.Exercise
import com.privatecoach.app.core.model.TrainingTemplate
import com.privatecoach.app.core.model.WorkoutType

data class ManualEntryUiState(
    val workoutType: WorkoutType = WorkoutType.STRENGTH,
    val exercises: List<Exercise> = listOf(Exercise(name = "", weightUnit = "kg")),
    val templates: List<TrainingTemplate> = emptyList(),
    val selectedTemplateId: Long? = null,
    val bodyPart: BodyPart? = null,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)
