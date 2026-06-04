package com.privatecoach.app.core.analytics

import com.privatecoach.app.core.model.ExerciseTrendPoint
import com.privatecoach.app.core.model.MultiExerciseTrend
import com.privatecoach.app.core.model.TrendPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendAnalyzer @Inject constructor() {

    fun computeTrends(raw: List<ExerciseTrendPoint>): List<MultiExerciseTrend> {
        return raw
            .groupBy { it.name }
            .map { (exerciseName, points) ->
                MultiExerciseTrend(
                    exerciseName = exerciseName,
                    points = computeTrendForExercise(points)
                )
            }
            .sortedBy { it.exerciseName }
    }

    fun computeTrendForExercise(raw: List<ExerciseTrendPoint>): List<TrendPoint> {
        return raw
            .groupBy { it.date }
            .map { (date, dayPoints) ->
                val weights = dayPoints.mapNotNull { it.weight }
                val maxWeight = weights.maxOrNull()
                val totalVolume = dayPoints.sumOf {
                    (it.weight ?: 0.0) * (it.sets ?: 0) * (it.reps ?: 0)
                }
                val repsList = dayPoints.mapNotNull { it.reps?.toDouble() }
                val avgReps = if (repsList.isNotEmpty()) repsList.average() else null

                TrendPoint(
                    date = date,
                    maxWeight = maxWeight,
                    totalVolume = if (totalVolume > 0) totalVolume else null,
                    avgReps = avgReps
                )
            }
            .sortedBy { it.date }
    }

    /**
     * Aggregates all exercises within a body part into a single trend line.
     * Groups by date, sums total volume across all exercises for each day.
     */
    fun computeBodyPartAggregation(raw: List<ExerciseTrendPoint>, label: String): MultiExerciseTrend {
        val points = raw
            .groupBy { it.date }
            .map { (date, dayPoints) ->
                val totalVolume = dayPoints.sumOf {
                    (it.weight ?: 0.0) * (it.sets ?: 0) * (it.reps ?: 0)
                }
                val maxWeight = dayPoints.mapNotNull { it.weight }.maxOrNull()
                TrendPoint(
                    date = date,
                    maxWeight = maxWeight,
                    totalVolume = if (totalVolume > 0) totalVolume else null,
                    avgReps = null
                )
            }
            .sortedBy { it.date }
        return MultiExerciseTrend(exerciseName = label, points = points)
    }
}
