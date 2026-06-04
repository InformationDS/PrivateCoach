package com.privatecoach.app.core.model

import java.time.Instant
import java.time.LocalDate

data class TrendPoint(
    val date: LocalDate,
    val maxWeight: Double?,
    val totalVolume: Double?,
    val avgReps: Double?
)

data class WeeklyVolume(
    val weekStart: LocalDate,
    val totalSets: Int,
    val totalVolume: Double,
    val trainingDays: Int
)

data class BodyPartStat(
    val bodyPart: String,
    val count: Int,
    val percentage: Float
)

// Raw data class returned from Room query for trend calculation
data class ExerciseTrendPoint(
    val name: String,
    val date: LocalDate,
    val weight: Double?,
    val sets: Int?,
    val reps: Int?
)

// Raw data class for body part distribution
data class BodyPartCount(
    val bodyPart: String,
    val count: Int
)

enum class TimeRange(val months: Int, val chineseName: String) {
    ONE_MONTH(1, "1个月"),
    THREE_MONTHS(3, "3个月"),
    SIX_MONTHS(6, "6个月"),
    ALL(Int.MAX_VALUE, "全部");
}

// Raw data class returned from Room query for volume aggregation
data class VolumeDataPoint(
    val workoutDate: LocalDate,
    val weight: Double,
    val sets: Int,
    val reps: Int
)

// Raw data class for training frequency
data class TrainingFrequencyPoint(
    val date: LocalDate,
    val sessions: Int
)

// UI-ready type for multi-exercise trend charting
data class MultiExerciseTrend(
    val exerciseName: String,
    val points: List<TrendPoint>
)

data class MonthlySummary(
    val totalSets: Int,
    val totalVolume: Double,
    val trainingDays: Int
)

data class Report(
    val title: String,
    val period: String,
    val trainingDays: Int,
    val totalExercises: Int,
    val volumeChange: Float?,
    val topBodyParts: List<BodyPartStat>,
    val topProgressExercises: List<String>,
    val aiSummary: String?,
    val generatedAt: Instant
)
