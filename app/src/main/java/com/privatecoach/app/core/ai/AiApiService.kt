package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.AdviceContext
import com.privatecoach.app.core.model.AdviceResult
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.QueryContext
import com.privatecoach.app.core.model.ReportInputData
import com.privatecoach.app.core.model.ReviewContext
import com.privatecoach.app.core.model.ReviewResult
import java.io.File

interface AiApiService {
    suspend fun transcribeAndParse(audioFile: File): Result<AiParsedResult>
    suspend fun parseText(text: String): Result<AiParsedResult>
    suspend fun generateReport(reportData: ReportInputData): Result<String>

    /** 生成个性化训练建议 (ADVICE intent) */
    suspend fun generateAdvice(context: AdviceContext): Result<AdviceResult>

    /** 生成训练复盘总结 (REVIEW intent) */
    suspend fun generateReview(context: ReviewContext): Result<ReviewResult>

    /** AI 解读复杂查询结果 (QUERY intent, 复杂时使用) */
    suspend fun interpretQuery(context: QueryContext): Result<String>
}
