package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.ParsedCardioDetail
import com.privatecoach.app.core.model.ParsedExercise
import com.privatecoach.app.core.model.WorkoutType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiResponseParser @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(jsonString: String): Result<AiParsedResult> = runCatching {
        // Try to extract JSON from markdown code blocks if present
        val cleanJson = jsonString
            .replace(Regex("```json\\s*"), "")
            .replace(Regex("```\\s*"), "")
            .trim()

        val root = json.parseToJsonElement(cleanJson).jsonObject

        val type = root["type"]?.jsonPrimitive?.content?.let {
            try { WorkoutType.valueOf(it.uppercase()) } catch (_: Exception) { WorkoutType.STRENGTH }
        } ?: WorkoutType.STRENGTH

        val bodyPartRaw = root["bodyPart"]?.jsonPrimitive?.content
        val bodyPart = bodyPartRaw?.let { BodyPart.fromChinese(it) }

        val exercises = root["exercises"]?.jsonArray?.map { elem ->
            val obj = elem.jsonObject
            val exFeeling = obj["feeling"]?.jsonPrimitive?.content?.let { f ->
                when (f) {
                    "轻松" -> com.privatecoach.app.core.model.Feeling.EASY
                    "良好" -> com.privatecoach.app.core.model.Feeling.GOOD
                    "一般" -> com.privatecoach.app.core.model.Feeling.NORMAL
                    "疲劳" -> com.privatecoach.app.core.model.Feeling.TIRED
                    else -> null
                }
            }
            ParsedExercise(
                name = obj["name"]?.jsonPrimitive?.content ?: "",
                weight = obj["weight"]?.jsonPrimitive?.doubleOrNull,
                weightUnit = obj["weightUnit"]?.jsonPrimitive?.content ?: "kg",
                sets = obj["sets"]?.jsonPrimitive?.intOrNull,
                reps = obj["reps"]?.jsonPrimitive?.intOrNull,
                duration = obj["duration"]?.jsonPrimitive?.intOrNull,
                distance = obj["distance"]?.jsonPrimitive?.doubleOrNull,
                feeling = exFeeling
            )
        } ?: emptyList()

        val cardioDetail = root["cardioDetail"]?.jsonObject?.let { cd ->
            ParsedCardioDetail(
                cardioType = cd["cardioType"]?.jsonPrimitive?.content,
                duration = cd["duration"]?.jsonPrimitive?.intOrNull,
                distance = cd["distance"]?.jsonPrimitive?.doubleOrNull,
                avgHeartRate = cd["avgHeartRate"]?.jsonPrimitive?.intOrNull,
                calories = cd["calories"]?.jsonPrimitive?.intOrNull
            )
        }

        val notes = root["notes"]?.jsonPrimitive?.content
        val summary = root["summary"]?.jsonPrimitive?.content ?: ""

        AiParsedResult(
            type = type,
            bodyPart = bodyPart,
            exercises = exercises,
            cardioDetail = cardioDetail,
            notes = notes,
            summaryMarkdown = summary,
            rawJson = cleanJson
        )
    }
}
