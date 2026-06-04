package com.privatecoach.app.ui.screen.record

import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.audio.RecordingState
import com.privatecoach.app.core.model.TrainingTemplate

data class RecordUiState(
    val recordingState: RecordingState = RecordingState.IDLE,
    val currentAmplitude: Int = 0,
    val elapsedSeconds: Int = 0,
    val audioFilePath: String? = null,
    val isProcessing: Boolean = false,
    val parsedResult: AiParsedResult? = null,
    val errorMessage: String? = null,
    val inputMode: InputTab = InputTab.VOICE,
    val textInput: String = "",
    val templates: List<TrainingTemplate> = emptyList(),
    val selectedTemplateName: String? = null
)

enum class InputTab { VOICE, TEXT, MANUAL }
