package com.privatecoach.app.data.mapper

import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.CardioDetail
import com.privatecoach.app.core.model.Exercise
import com.privatecoach.app.core.model.ExportCardioDetail
import com.privatecoach.app.core.model.ExportExercise
import com.privatecoach.app.core.model.ExportTemplate
import com.privatecoach.app.core.model.ExportTemplateExercise
import com.privatecoach.app.core.model.ExportWorkout
import com.privatecoach.app.core.model.Feeling
import com.privatecoach.app.core.model.InputMode
import com.privatecoach.app.core.model.TemplateExercise
import com.privatecoach.app.core.model.TrainingTemplate
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.core.model.WorkoutType
import java.time.Instant
import java.time.LocalDate

// ─── Domain → Export ────────────────────────────────────────

fun Workout.toExportWorkout(): ExportWorkout = ExportWorkout(
    syncId = syncId,
    date = date.toString(),
    type = type.name,
    bodyPart = bodyPart?.name,
    aiSummary = aiSummary,
    rawTranscript = rawTranscript,
    inputMode = inputMode.name,
    createdAt = createdAt.toString(),
    exercises = exercises.map { it.toExportExercise() }
)

fun Exercise.toExportExercise(): ExportExercise = ExportExercise(
    name = name,
    weight = weight,
    weightUnit = weightUnit,
    sets = sets,
    reps = reps,
    duration = duration,
    distance = distance,
    sortOrder = sortOrder,
    notes = notes,
    feeling = feeling?.name,
    cardioDetail = cardioDetail?.toExportCardioDetail()
)

fun CardioDetail.toExportCardioDetail(): ExportCardioDetail = ExportCardioDetail(
    durationSeconds = durationSeconds,
    distanceKm = distanceKm,
    avgHeartRate = avgHeartRate,
    calories = calories,
    cardioType = cardioType
)

fun TrainingTemplate.toExportTemplate(): ExportTemplate = ExportTemplate(
    name = name,
    type = type.name,
    bodyPart = bodyPart?.name,
    sortOrder = sortOrder,
    createdAt = createdAt.toString(),
    exercises = exercises.map { it.toExportTemplateExercise() }
)

fun TemplateExercise.toExportTemplateExercise(): ExportTemplateExercise = ExportTemplateExercise(
    name = name,
    weightUnit = weightUnit,
    sortOrder = sortOrder,
    notes = notes
)

// ─── Export → Domain ────────────────────────────────────────

fun ExportWorkout.toWorkout(): Workout = Workout(
    syncId = syncId,
    date = runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now()),
    type = runCatching { WorkoutType.valueOf(type) }.getOrDefault(WorkoutType.STRENGTH),
    bodyPart = bodyPart?.let { BodyPart.fromString(it) },
    aiSummary = aiSummary,
    rawTranscript = rawTranscript,
    inputMode = runCatching { InputMode.valueOf(inputMode) }.getOrDefault(InputMode.MANUAL),
    createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
    updatedAt = Instant.now(),
    exercises = exercises.map { it.toExercise() }
)

fun ExportExercise.toExercise(): Exercise = Exercise(
    name = name,
    weight = weight,
    weightUnit = weightUnit,
    sets = sets,
    reps = reps,
    duration = duration,
    distance = distance,
    sortOrder = sortOrder,
    notes = notes,
    feeling = feeling?.let { runCatching { Feeling.valueOf(it) }.getOrNull() },
    cardioDetail = cardioDetail?.let {
        CardioDetail(
            durationSeconds = it.durationSeconds,
            distanceKm = it.distanceKm,
            avgHeartRate = it.avgHeartRate,
            calories = it.calories,
            cardioType = it.cardioType
        )
    }
)

fun ExportTemplate.toTrainingTemplate(): TrainingTemplate = TrainingTemplate(
    name = name,
    type = runCatching { WorkoutType.valueOf(type) }.getOrDefault(WorkoutType.STRENGTH),
    bodyPart = bodyPart?.let { BodyPart.fromString(it) },
    sortOrder = sortOrder,
    createdAt = runCatching { Instant.parse(createdAt) }.getOrDefault(Instant.now()),
    exercises = exercises.map { it.toTemplateExercise() }
)

fun ExportTemplateExercise.toTemplateExercise(): TemplateExercise = TemplateExercise(
    name = name,
    weightUnit = weightUnit,
    sortOrder = sortOrder,
    notes = notes
)
