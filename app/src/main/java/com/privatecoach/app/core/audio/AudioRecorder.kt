package com.privatecoach.app.core.audio

import kotlinx.coroutines.flow.StateFlow
import java.io.File

enum class RecordingState { IDLE, RECORDING, PAUSED, STOPPED, ERROR }

interface AudioRecorder {
    val recordingState: StateFlow<RecordingState>
    val currentAmplitude: StateFlow<Int>

    suspend fun startRecording(outputFile: File)
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun stopRecording(): File
    fun release()
}
