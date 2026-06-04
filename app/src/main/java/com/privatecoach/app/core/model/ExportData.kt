package com.privatecoach.app.core.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ExportContainer(
    val version: Int = 1,
    val exportedAt: String,
    val appVersion: String = "1.0.0",
    val workouts: List<ExportWorkout>,
    val templates: List<ExportTemplate>
)

@Serializable
data class ExportWorkout(
    val syncId: String? = null,
    val date: String,
    val type: String,
    val bodyPart: String? = null,
    val aiSummary: String? = null,
    val rawTranscript: String? = null,
    val inputMode: String,
    val createdAt: String,
    val exercises: List<ExportExercise>
)

@Serializable
data class ExportExercise(
    val name: String,
    val weight: Double? = null,
    val weightUnit: String = "kg",
    val sets: Int? = null,
    val reps: Int? = null,
    val duration: Int? = null,
    val distance: Double? = null,
    val sortOrder: Int = 0,
    val notes: String? = null,
    val feeling: String? = null,
    val cardioDetail: ExportCardioDetail? = null
)

@Serializable
data class ExportCardioDetail(
    val durationSeconds: Int,
    val distanceKm: Double? = null,
    val avgHeartRate: Int? = null,
    val calories: Int? = null,
    val cardioType: String? = null
)

@Serializable
data class ExportTemplate(
    val name: String,
    val type: String,
    val bodyPart: String? = null,
    val sortOrder: Int = 0,
    val createdAt: String,
    val exercises: List<ExportTemplateExercise>
)

@Serializable
data class ExportTemplateExercise(
    val name: String,
    val weightUnit: String = "kg",
    val sortOrder: Int = 0,
    val notes: String? = null
)

data class ImportResult(
    val workoutsImported: Int,
    val templatesImported: Int,
    val errors: List<String>
)
