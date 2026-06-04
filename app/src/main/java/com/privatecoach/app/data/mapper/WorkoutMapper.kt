package com.privatecoach.app.data.mapper

import com.privatecoach.app.core.model.CardioDetail
import com.privatecoach.app.core.model.Exercise
import com.privatecoach.app.core.model.Feeling
import com.privatecoach.app.core.model.InputMode
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.data.local.entity.CardioDetailEntity
import com.privatecoach.app.data.local.entity.ExerciseEntity
import com.privatecoach.app.data.local.entity.WorkoutEntity
import com.privatecoach.app.data.local.relation.ExerciseWithCardioDetail
import com.privatecoach.app.data.local.relation.WorkoutWithExercises

fun WorkoutWithExercises.toDomain(): Workout = Workout(
    id = workout.id,
    syncId = workout.syncId,
    date = workout.date,
    type = workout.type,
    bodyPart = workout.bodyPart,
    feeling = workout.feeling?.let { runCatching { Feeling.valueOf(it) }.getOrNull() },
    aiSummary = workout.aiSummary,
    rawTranscript = workout.rawTranscript,
    audioFilePath = workout.audioFilePath,
    inputMode = workout.inputMode,
    templateId = workout.templateId,
    createdAt = workout.createdAt,
    updatedAt = workout.updatedAt,
    exercises = exercises.map { it.toDomain() }
)

fun ExerciseWithCardioDetail.toDomain(): Exercise = Exercise(
    id = exercise.id,
    workoutId = exercise.workoutId,
    name = exercise.name,
    weight = exercise.weight,
    weightUnit = exercise.weightUnit,
    sets = exercise.sets,
    reps = exercise.reps,
    duration = exercise.duration,
    distance = exercise.distance,
    sortOrder = exercise.sortOrder,
    notes = exercise.notes,
    cardioDetail = cardioDetail?.toDomain()
)

fun CardioDetailEntity.toDomain(): CardioDetail = CardioDetail(
    id = id,
    exerciseId = exerciseId,
    durationSeconds = durationSeconds,
    distanceKm = distanceKm,
    avgHeartRate = avgHeartRate,
    calories = calories,
    cardioType = cardioType
)

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    syncId = syncId,
    date = date,
    type = type,
    bodyPart = bodyPart,
    feeling = feeling?.name,
    aiSummary = aiSummary,
    rawTranscript = rawTranscript,
    audioFilePath = audioFilePath,
    inputMode = inputMode,
    templateId = templateId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Exercise.toEntity(workoutId: Long): ExerciseEntity = ExerciseEntity(
    id = id,
    workoutId = workoutId,
    name = name,
    weight = weight,
    weightUnit = weightUnit,
    sets = sets,
    reps = reps,
    duration = duration,
    distance = distance,
    sortOrder = sortOrder,
    notes = notes
)

fun CardioDetail.toEntity(): CardioDetailEntity = CardioDetailEntity(
    id = id,
    exerciseId = exerciseId,
    durationSeconds = durationSeconds,
    distanceKm = distanceKm,
    avgHeartRate = avgHeartRate,
    calories = calories,
    cardioType = cardioType
)
