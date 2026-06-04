package com.privatecoach.app.ui.screen.record

import com.privatecoach.app.core.model.AiParsedResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple in-memory holder to pass AI result between RecordScreen and ConfirmResultScreen.
 */
@Singleton
class AiResultHolder @Inject constructor() {
    var pendingResult: AiParsedResult? = null
    var pendingAudioPath: String? = null

    fun set(result: AiParsedResult, audioPath: String?) {
        pendingResult = result
        pendingAudioPath = audioPath
    }

    fun consume(): Pair<AiParsedResult?, String?> {
        val result = pendingResult
        val audio = pendingAudioPath
        pendingResult = null
        pendingAudioPath = null
        return result to audio
    }
}
