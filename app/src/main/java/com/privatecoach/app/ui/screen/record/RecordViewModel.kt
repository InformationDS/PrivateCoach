package com.privatecoach.app.ui.screen.record

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.core.ai.AiApiService
import com.privatecoach.app.core.audio.AudioRecorder
import com.privatecoach.app.core.audio.RecordingState
import com.privatecoach.app.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    application: Application,
    private val audioRecorder: AudioRecorder,
    private val aiApiService: AiApiService,
    private val templateRepository: TemplateRepository,
    private val resultHolder: AiResultHolder
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Collect recording state
        viewModelScope.launch {
            audioRecorder.recordingState.collect { state ->
                _uiState.update { it.copy(recordingState = state) }
            }
        }
        viewModelScope.launch {
            audioRecorder.currentAmplitude.collect { amp ->
                _uiState.update { it.copy(currentAmplitude = amp) }
            }
        }
        // Load templates
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                _uiState.update { it.copy(templates = templates) }
            }
        }
    }

    fun setInputMode(mode: InputTab) {
        _uiState.update { it.copy(inputMode = mode, errorMessage = null, parsedResult = null) }
    }

    fun setTextInput(text: String) {
        _uiState.update { it.copy(textInput = text) }
    }

    fun setTemplate(templateName: String?) {
        _uiState.update { it.copy(selectedTemplateName = templateName) }
    }

    fun startRecording() {
        val file = File(getApplication<Application>().filesDir, "audio/recording_${System.currentTimeMillis()}.m4a")
        file.parentFile?.mkdirs()

        viewModelScope.launch {
            try {
                audioRecorder.startRecording(file)
                _uiState.update { it.copy(audioFilePath = file.absolutePath, errorMessage = null, elapsedSeconds = 0) }
                startTimer()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "录音启动失败: ${e.message}") }
            }
        }
    }

    fun pauseRecording() {
        viewModelScope.launch {
            audioRecorder.pauseRecording()
            timerJob?.cancel()
        }
    }

    fun resumeRecording() {
        viewModelScope.launch {
            audioRecorder.resumeRecording()
            startTimer()
        }
    }

    fun stopRecording() {
        timerJob?.cancel()
        viewModelScope.launch {
            try {
                val file = audioRecorder.stopRecording()
                _uiState.update { it.copy(isProcessing = true) }

                val result = aiApiService.transcribeAndParse(file)
                result.fold(
                    onSuccess = { parsed ->
                        resultHolder.set(parsed, file.absolutePath)
                        _uiState.update { it.copy(isProcessing = false, parsedResult = parsed) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isProcessing = false, errorMessage = error.message ?: "AI 解析失败") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, errorMessage = e.message ?: "处理失败") }
            }
        }
    }

    fun sendTextToAi() {
        val text = _uiState.value.textInput.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            val result = aiApiService.parseText(text)
            result.fold(
                onSuccess = { parsed ->
                    resultHolder.set(parsed, null)
                    _uiState.update { it.copy(isProcessing = false, parsedResult = parsed) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isProcessing = false, errorMessage = error.message ?: "AI 解析失败") }
                }
            )
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(parsedResult = null, audioFilePath = null, textInput = "", elapsedSeconds = 0) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.release()
    }
}
