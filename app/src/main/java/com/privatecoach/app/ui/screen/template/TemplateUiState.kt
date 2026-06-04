package com.privatecoach.app.ui.screen.template

import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.TrainingTemplate
import com.privatecoach.app.core.model.TemplateExercise
import com.privatecoach.app.core.model.WorkoutType

data class TemplateListUiState(
    val templates: List<TrainingTemplate> = emptyList(),
    val isLoading: Boolean = true
)

data class TemplateEditUiState(
    val name: String = "",
    val type: WorkoutType = WorkoutType.STRENGTH,
    val bodyPart: BodyPart? = null,
    val exercises: List<TemplateExercise> = listOf(TemplateExercise(name = "", weightUnit = "kg")),
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)
