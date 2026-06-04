package com.privatecoach.app.core.audio

import android.media.MediaRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorderImpl @Inject constructor() : AudioRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var amplitudeMonitor: Boolean = false

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0)
    override val currentAmplitude: StateFlow<Int> = _currentAmplitude.asStateFlow()

    override suspend fun startRecording(outputFile: File) {
        this.outputFile = outputFile
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setAudioChannels(1)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
        _recordingState.value = RecordingState.RECORDING
        startAmplitudeMonitor()
    }

    override suspend fun pauseRecording() {
        mediaRecorder?.pause()
        _recordingState.value = RecordingState.PAUSED
        amplitudeMonitor = false
    }

    override suspend fun resumeRecording() {
        mediaRecorder?.resume()
        _recordingState.value = RecordingState.RECORDING
        startAmplitudeMonitor()
    }

    override suspend fun stopRecording(): File {
        amplitudeMonitor = false
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {}
        mediaRecorder?.release()
        mediaRecorder = null
        _recordingState.value = RecordingState.STOPPED
        return outputFile ?: throw IllegalStateException("No output file")
    }

    override fun release() {
        amplitudeMonitor = false
        try { mediaRecorder?.stop() } catch (_: Exception) {}
        mediaRecorder?.release()
        mediaRecorder = null
        _recordingState.value = RecordingState.IDLE
    }

    private fun startAmplitudeMonitor() {
        amplitudeMonitor = true
        Thread {
            while (amplitudeMonitor) {
                try {
                    val amp = mediaRecorder?.maxAmplitude ?: 0
                    // Normalize: maxAmplitude is 0-32767, scale to 0-100
                    _currentAmplitude.value = ((amp.toFloat() / 32767f) * 100f).toInt()
                    Thread.sleep(50) // ~20fps
                } catch (_: Exception) { break }
            }
        }.start()
    }
}
