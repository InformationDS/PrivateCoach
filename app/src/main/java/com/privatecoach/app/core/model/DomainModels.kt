package com.privatecoach.app.core.model

import java.time.Instant
import java.time.LocalDate

enum class SameDayWriteMode { APPEND, OVERWRITE }

data class Workout(
    val id: Long = 0,
    val syncId: String? = null,
    val date: LocalDate,
    val type: WorkoutType,
    val bodyPart: BodyPart? = null,
    val aiSummary: String? = null,
    val rawTranscript: String? = null,
    val audioFilePath: String? = null,
    val inputMode: InputMode,
    val templateId: Long? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val exercises: List<Exercise> = emptyList()
)

data class Exercise(
    val id: Long = 0,
    val workoutId: Long = 0,
    val name: String,
    val weight: Double? = null,
    val weightUnit: String = "kg",
    val sets: Int? = null,
    val reps: Int? = null,
    val duration: Int? = null,
    val distance: Double? = null,
    val sortOrder: Int = 0,
    val notes: String? = null,
    val feeling: Feeling? = null,
    val cardioDetail: CardioDetail? = null
)

data class CardioDetail(
    val id: Long = 0,
    val exerciseId: Long = 0,
    val durationSeconds: Int,
    val distanceKm: Double? = null,
    val avgHeartRate: Int? = null,
    val calories: Int? = null,
    val cardioType: String? = null
)

data class TrainingTemplate(
    val id: Long = 0,
    val name: String,
    val type: WorkoutType,
    val bodyPart: BodyPart? = null,
    val sortOrder: Int = 0,
    val createdAt: Instant = Instant.now(),
    val exercises: List<TemplateExercise> = emptyList()
)

data class TemplateExercise(
    val id: Long = 0,
    val templateId: Long = 0,
    val name: String,
    val weightUnit: String = "kg",
    val sortOrder: Int = 0,
    val notes: String? = null
)
