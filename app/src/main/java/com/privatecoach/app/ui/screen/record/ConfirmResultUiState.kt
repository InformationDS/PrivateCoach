package com.privatecoach.app.ui.screen.record

import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.ParsedExercise

data class ConfirmResultUiState(
    val parsedResult: AiParsedResult? = null,
    val exercises: List<ParsedExercise> = emptyList(),
    val bodyPart: String = "",
    val feeling: String? = null,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val audioFilePath: String? = null
)
