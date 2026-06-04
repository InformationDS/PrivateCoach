package com.privatecoach.app.core.audio

import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class PlaybackState { IDLE, PLAYING, PAUSED, COMPLETED, ERROR }

@Singleton
class AudioPlayer @Inject constructor() {

    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(filePath: String) {
        release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                _playbackState.value = PlaybackState.PLAYING
                setOnCompletionListener {
                    _playbackState.value = PlaybackState.COMPLETED
                }
                setOnErrorListener { _, _, _ ->
                    _playbackState.value = PlaybackState.ERROR
                    true
                }
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.ERROR
            }
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _playbackState.value = PlaybackState.PAUSED
    }

    fun resume() {
        mediaPlayer?.start()
        _playbackState.value = PlaybackState.PLAYING
    }

    fun stop() {
        mediaPlayer?.stop()
        release()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = PlaybackState.IDLE
    }
}
