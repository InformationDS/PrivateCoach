package com.privatecoach.app.core.model

data class AiParsedResult(
    val type: WorkoutType,
    val bodyPart: String?,
    val exercises: List<ParsedExercise>,
    val cardioDetail: ParsedCardioDetail?,
    val feeling: Feeling?,
    val notes: String?,
    val summaryMarkdown: String,
    val rawJson: String
)

data class ParsedExercise(
    val name: String,
    val weight: Double?,
    val weightUnit: String,
    val sets: Int?,
    val reps: Int?,
    val duration: Int?,
    val distance: Double?
)

data class ParsedCardioDetail(
    val cardioType: String?,
    val duration: Int?,
    val distance: Double?,
    val avgHeartRate: Int?,
    val calories: Int?
)

data class ReportInputData(
    val periodName: String,
    val trainingDays: Int,
    val totalExercises: Int,
    val volumeChange: Float?,
    val topBodyParts: List<BodyPartStat>,
    val previousPeriodName: String,
    val progressExercises: List<String>
)
