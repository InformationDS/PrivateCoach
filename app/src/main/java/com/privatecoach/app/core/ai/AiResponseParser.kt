package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.AdviceResult
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.DataSufficiency
import com.privatecoach.app.core.model.ParsedCardioDetail
import com.privatecoach.app.core.model.ParsedExercise
import com.privatecoach.app.core.model.ReviewResult
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

        var exercises = root["exercises"]?.jsonArray?.map { elem ->
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

        if (type == WorkoutType.CARDIO && exercises.isEmpty() && cardioDetail != null) {
            exercises = listOf(
                ParsedExercise(
                    name = cardioDetail.cardioType?.takeIf { it.isNotBlank() } ?: "有氧训练",
                    weight = null,
                    weightUnit = "kg",
                    sets = null,
                    reps = null,
                    duration = cardioDetail.duration,
                    distance = cardioDetail.distance
                )
            )
        }
        require(exercises.isNotEmpty()) { "AI 返回中没有有效动作" }
        require(exercises.all { it.name.isNotBlank() }) { "AI 返回的动作名称为空" }
        require(exercises.all {
            (it.weight == null || it.weight > 0) &&
                (it.sets == null || it.sets > 0) &&
                (it.reps == null || it.reps > 0) &&
                (it.duration == null || it.duration > 0) &&
                (it.distance == null || it.distance > 0)
        }) { "AI 返回包含非法数值" }
        require(cardioDetail?.avgHeartRate == null || cardioDetail.avgHeartRate in 30..240) { "AI 返回的心率不合理" }

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

    fun parseAdvice(jsonString: String): Result<AdviceResult> = runCatching {
        val cleanJson = jsonString
            .replace(Regex("```json\\s*"), "")
            .replace(Regex("```\\s*"), "")
            .trim()

        val root = json.parseToJsonElement(cleanJson).jsonObject

        val dataSummary = root["dataSummary"]?.jsonPrimitive?.content ?: ""
        val analysis = root["analysis"]?.jsonPrimitive?.content ?: ""
        val suggestions = root["suggestions"]?.jsonArray?.map {
            it.jsonPrimitive.content
        } ?: emptyList()

        val sufficiencyRaw = root["dataSufficiency"]?.jsonPrimitive?.content
        val dataSufficiency = when (sufficiencyRaw?.uppercase()) {
            "SUFFICIENT" -> DataSufficiency.SUFFICIENT
            "LIMITED" -> DataSufficiency.LIMITED
            "INSUFFICIENT" -> DataSufficiency.INSUFFICIENT
            else -> DataSufficiency.LIMITED
        }

        AdviceResult(
            dataSummary = dataSummary,
            analysis = analysis,
            suggestions = suggestions,
            dataSufficiency = dataSufficiency
        )
    }

    fun parseReview(jsonString: String): Result<ReviewResult> = runCatching {
        val cleanJson = jsonString
            .replace(Regex("```json\\s*"), "")
            .replace(Regex("```\\s*"), "")
            .trim()

        val root = json.parseToJsonElement(cleanJson).jsonObject

        val overview = root["overview"]?.jsonPrimitive?.content ?: ""
        val highlights = root["highlights"]?.jsonArray?.map {
            it.jsonPrimitive.content
        } ?: emptyList()
        val concerns = root["concerns"]?.jsonArray?.map {
            it.jsonPrimitive.content
        } ?: emptyList()
        val comparisonText = root["comparisonText"]?.jsonPrimitive?.content ?: ""

        ReviewResult(
            overview = overview,
            highlights = highlights,
            concerns = concerns,
            comparisonText = comparisonText
        )
    }
}
