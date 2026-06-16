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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
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
            val apiKey = settingsRepository.apiKey.first()
            val modelName = settingsRepository.modelName.first()
            val baseEndpoint = settingsRepository.apiEndpoint.first()

            if (apiKey.isBlank()) throw IllegalStateException("请先在设置中配置 API Key")

            val audioBytes = audioFile.readBytes()
            val base64Audio = Base64.getEncoder().encodeToString(audioBytes)

            val systemInstruction = buildJsonObject {
                putJsonArray("parts") {
                    addJsonObject { put("text", AiPromptBuilder.buildSystemPrompt()) }
                }
            }

            val audioPart = buildJsonObject {
                putJsonObject("inline_data") {
                    put("mime_type", "audio/mp4")
                    put("data", base64Audio)
                }
            }

            val requestBody = buildJsonObject {
                put("system_instruction", systemInstruction)
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject { put("text", "请解析这段训练录音") }
                            add(audioPart)
                        }
                    }
                }
                putJsonObject("generation_config") {
                    put("response_mime_type", "application/json")
                }
            }

            val url = "$baseEndpoint/$modelName:generateContent?key=$apiKey"
            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
            ).execute()

            val responseBody = response.body?.string() ?: throw IllegalStateException("API 返回为空")
            if (!response.isSuccessful) throw IllegalStateException("API 错误 ${response.code}: $responseBody")

            val result = json.parseToJsonElement(responseBody) as JsonObject
            val text = result["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: throw IllegalStateException("无法解析 AI 返回结果")

            responseParser.parse(text).getOrThrow()
        }
    }

    override suspend fun parseText(text: String): Result<AiParsedResult> {
        return runCatching {
            val apiKey = settingsRepository.apiKey.first()
            val modelName = settingsRepository.modelName.first()
            val baseEndpoint = settingsRepository.apiEndpoint.first()

            if (apiKey.isBlank()) throw IllegalStateException("请先在设置中配置 API Key")

            val systemInstruction = buildJsonObject {
                putJsonArray("parts") {
                    addJsonObject { put("text", AiPromptBuilder.buildSystemPrompt()) }
                }
            }

            val requestBody = buildJsonObject {
                put("system_instruction", systemInstruction)
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject { put("text", AiPromptBuilder.buildUserPrompt(text)) }
                        }
                    }
                }
                putJsonObject("generation_config") {
                    put("response_mime_type", "application/json")
                }
            }

            val url = "$baseEndpoint/$modelName:generateContent?key=$apiKey"
            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
            ).execute()

            val responseBody = response.body?.string() ?: throw IllegalStateException("API 返回为空")
            if (!response.isSuccessful) throw IllegalStateException("API 错误 ${response.code}: $responseBody")

            val result = json.parseToJsonElement(responseBody) as JsonObject
            val responseText = result["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: throw IllegalStateException("无法解析 AI 返回结果")

            responseParser.parse(responseText).getOrThrow()
        }
    }

    override suspend fun generateReport(reportData: ReportInputData): Result<String> {
        return runCatching {
            val apiKey = settingsRepository.apiKey.first()
            val modelName = settingsRepository.modelName.first()
            val baseEndpoint = settingsRepository.apiEndpoint.first()

            if (apiKey.isBlank()) return@runCatching ""

            val requestBody = buildJsonObject {
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject { put("text", AiPromptBuilder.buildReportPrompt(reportData)) }
                        }
                    }
                }
            }

            val url = "$baseEndpoint/$modelName:generateContent?key=$apiKey"
            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
            ).execute()

            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) return@runCatching ""

            val result = json.parseToJsonElement(responseBody) as JsonObject
            result["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: ""
        }
    }

    override suspend fun generateAdvice(context: AdviceContext): Result<AdviceResult> {
        return runCatching {
            val apiKey = settingsRepository.apiKey.first()
            val modelName = settingsRepository.modelName.first()
            val baseEndpoint = settingsRepository.apiEndpoint.first()

            if (apiKey.isBlank()) throw IllegalStateException("请先在设置中配置 API Key")

            val systemInstruction = buildJsonObject {
                putJsonArray("parts") {
                    addJsonObject { put("text", AiPromptBuilder.buildAdviceSystemPrompt()) }
                }
            }

            val requestBody = buildJsonObject {
                put("system_instruction", systemInstruction)
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject {
                                put("text", AiPromptBuilder.buildAdviceUserPrompt(context))
                            }
                        }
                    }
                }
                putJsonObject("generation_config") {
                    put("response_mime_type", "application/json")
                }
            }

            val url = "$baseEndpoint/$modelName:generateContent?key=$apiKey"
            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
            ).execute()

            val responseBody = response.body?.string()
                ?: throw IllegalStateException("API 返回为空")
            if (!response.isSuccessful)
                throw IllegalStateException("API 错误 ${response.code}: $responseBody")

            val result = json.parseToJsonElement(responseBody) as JsonObject
            val text = result["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: throw IllegalStateException("无法解析 AI 返回结果")

            responseParser.parseAdvice(text).getOrThrow()
        }
    }

    override suspend fun generateReview(context: ReviewContext): Result<ReviewResult> {
        return runCatching {
            val apiKey = settingsRepository.apiKey.first()
            val modelName = settingsRepository.modelName.first()
            val baseEndpoint = settingsRepository.apiEndpoint.first()

            if (apiKey.isBlank()) throw IllegalStateException("请先在设置中配置 API Key")

            val systemInstruction = buildJsonObject {
                putJsonArray("parts") {
                    addJsonObject { put("text", AiPromptBuilder.buildReviewSystemPrompt()) }
                }
            }

            val requestBody = buildJsonObject {
                put("system_instruction", systemInstruction)
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject {
                                put("text", AiPromptBuilder.buildReviewUserPrompt(context))
                            }
                        }
                    }
                }
                putJsonObject("generation_config") {
                    put("response_mime_type", "application/json")
                }
            }

            val url = "$baseEndpoint/$modelName:generateContent?key=$apiKey"
            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
            ).execute()

            val responseBody = response.body?.string()
                ?: throw IllegalStateException("API 返回为空")
            if (!response.isSuccessful)
                throw IllegalStateException("API 错误 ${response.code}: $responseBody")

            val result = json.parseToJsonElement(responseBody) as JsonObject
            val text = result["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: throw IllegalStateException("无法解析 AI 返回结果")

            responseParser.parseReview(text).getOrThrow()
        }
    }

    override suspend fun interpretQuery(context: QueryContext): Result<String> {
        return runCatching {
            val apiKey = settingsRepository.apiKey.first()
            val modelName = settingsRepository.modelName.first()
            val baseEndpoint = settingsRepository.apiEndpoint.first()

            if (apiKey.isBlank()) return@runCatching ""

            val requestBody = buildJsonObject {
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject {
                                put("text", AiPromptBuilder.buildInterpretQueryPrompt(context))
                            }
                        }
                    }
                }
            }

            val url = "$baseEndpoint/$modelName:generateContent?key=$apiKey"
            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
            ).execute()

            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) return@runCatching ""

            val result = json.parseToJsonElement(responseBody) as JsonObject
            result["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: ""
        }
    }
}
