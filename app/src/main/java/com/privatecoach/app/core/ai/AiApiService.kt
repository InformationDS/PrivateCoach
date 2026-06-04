package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.ReportInputData
import java.io.File

interface AiApiService {
    suspend fun transcribeAndParse(audioFile: File): Result<AiParsedResult>
    suspend fun parseText(text: String): Result<AiParsedResult>
    suspend fun generateReport(reportData: ReportInputData): Result<String>
}
