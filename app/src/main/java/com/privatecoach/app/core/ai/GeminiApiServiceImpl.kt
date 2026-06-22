package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.AdviceContext
import com.privatecoach.app.core.model.AdviceResult
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.QueryContext
import com.privatecoach.app.core.model.ReportInputData
import com.privatecoach.app.core.model.ReviewContext
import com.privatecoach.app.core.model.ReviewResult
import com.privatecoach.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiServiceImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val responseParser: AiResponseParser
) : AiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun transcribeAndParse(audioFile: File): Result<AiParsedResult> {
        return runCatching {
            val text = requestChatCompletion(
                systemPrompt = AiPromptBuilder.buildSystemPrompt(),
                userContent = buildJsonArray {
                    addJsonObject {
                        put("type", "text")
                        put("text", "请识别这段训练录音，并按要求输出结构化 JSON。")
                    }
                    addJsonObject {
                        put("type", "input_audio")
                        putJsonObject("input_audio") {
                            put("data", Base64.getEncoder().encodeToString(audioFile.readBytes()))
                            put("format", inferAudioFormat(audioFile))
                        }
                    }
                }
            )
            responseParser.parse(text).getOrThrow()
        }
    }

    override suspend fun parseText(text: String): Result<AiParsedResult> {
        return runCatching {
            val responseText = requestChatCompletion(
                systemPrompt = AiPromptBuilder.buildSystemPrompt(),
                userContent = AiPromptBuilder.buildUserPrompt(text)
            )
            responseParser.parse(responseText).getOrThrow()
        }
    }

    override suspend fun generateReport(reportData: ReportInputData): Result<String> {
        return runCatching {
            if (settingsRepository.apiKey.first().isBlank()) return@runCatching ""
            requestChatCompletion(
                userContent = AiPromptBuilder.buildReportPrompt(reportData)
            )
        }
    }

    override suspend fun generateAdvice(context: AdviceContext): Result<AdviceResult> {
        return runCatching {
            val text = requestChatCompletion(
                systemPrompt = AiPromptBuilder.buildAdviceSystemPrompt(),
                userContent = AiPromptBuilder.buildAdviceUserPrompt(context)
            )
            responseParser.parseAdvice(text).getOrThrow()
        }
    }

    override suspend fun generateReview(context: ReviewContext): Result<ReviewResult> {
        return runCatching {
            val text = requestChatCompletion(
                systemPrompt = AiPromptBuilder.buildReviewSystemPrompt(),
                userContent = AiPromptBuilder.buildReviewUserPrompt(context)
            )
            responseParser.parseReview(text).getOrThrow()
        }
    }

    override suspend fun interpretQuery(context: QueryContext): Result<String> {
        return runCatching {
            if (settingsRepository.apiKey.first().isBlank()) return@runCatching ""
            requestChatCompletion(
                userContent = AiPromptBuilder.buildInterpretQueryPrompt(context)
            )
        }
    }

    private suspend fun requestChatCompletion(
        systemPrompt: String? = null,
        userContent: String
    ): String {
        return requestChatCompletionInternal(systemPrompt, userContent)
    }

    private suspend fun requestChatCompletion(
        systemPrompt: String? = null,
        userContent: JsonElement
    ): String {
        return requestChatCompletionInternal(systemPrompt, userContent)
    }

    private suspend fun requestChatCompletionInternal(
        systemPrompt: String? = null,
        userContent: Any
    ): String {
        val apiKey = settingsRepository.apiKey.first()
        val modelName = settingsRepository.modelName.first()
        val baseEndpoint = settingsRepository.apiEndpoint.first().trimEnd('/')
        ensureConfigured(apiKey, modelName, baseEndpoint)

        val requestBody = buildJsonObject {
            put("model", modelName)
            put("stream", false)
            put("temperature", 0.2)
            put("top_p", 0.95)
            put("max_completion_tokens", 2048)
            putJsonArray("messages") {
                if (!systemPrompt.isNullOrBlank()) {
                    addJsonObject {
                        put("role", "system")
                        put("content", systemPrompt)
                    }
                }
                addJsonObject {
                    put("role", "user")
                    when (userContent) {
                        is String -> put("content", userContent)
                        is JsonElement -> put("content", userContent)
                        else -> error("Unsupported user content type")
                    }
                }
            }
        }

        val responseBody = executeJsonPost(
            url = "$baseEndpoint/chat/completions",
            requestBody = requestBody,
            headers = mapOf(
                "api-key" to apiKey,
                "Authorization" to "Bearer $apiKey"
            )
        )

        val result = json.parseToJsonElement(responseBody) as JsonObject
        val content = result["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?: throw IllegalStateException("无法解析 AI 返回结果")

        return extractTextContent(content)
    }

    private fun executeJsonPost(
        url: String,
        requestBody: JsonObject,
        headers: Map<String, String>
    ): String {
        val builder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")

        headers.forEach { (name, value) -> builder.addHeader(name, value) }

        val response = client.newCall(
            builder
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
        ).execute()

        val responseBody = response.body?.string() ?: throw IllegalStateException("API 返回为空")
        if (!response.isSuccessful) throw IllegalStateException("API 错误 ${response.code}: $responseBody")
        return responseBody
    }

    private fun extractTextContent(content: JsonElement): String {
        return when (content) {
            is JsonArray -> content.joinToString("\n") { item ->
                item.jsonObject["text"]?.jsonPrimitive?.content.orEmpty()
            }
            else -> content.jsonPrimitive.content
        }.trim().ifBlank { throw IllegalStateException("无法解析 AI 返回结果") }
    }

    private fun ensureConfigured(apiKey: String, modelName: String, baseEndpoint: String) {
        if (apiKey.isBlank()) throw IllegalStateException("请先在设置中配置 API Key")
        if (modelName.isBlank()) throw IllegalStateException("请先在设置中配置模型名称")
        if (baseEndpoint.isBlank()) throw IllegalStateException("请先在设置中配置 API Endpoint")
    }

    private fun inferAudioFormat(audioFile: File): String {
        return when (audioFile.extension.lowercase()) {
            "mp3" -> "mp3"
            "wav" -> "wav"
            "m4a", "mp4" -> "mp4"
            else -> "mp4"
        }
    }
}
