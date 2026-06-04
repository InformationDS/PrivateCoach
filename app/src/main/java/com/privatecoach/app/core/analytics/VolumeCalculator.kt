package com.privatecoach.app.core.analytics

import com.privatecoach.app.core.model.BodyPartCount
import com.privatecoach.app.core.model.BodyPartStat
import com.privatecoach.app.core.model.MonthlySummary
import com.privatecoach.app.core.model.VolumeDataPoint
import com.privatecoach.app.core.model.WeeklyVolume
import com.privatecoach.app.core.model.Workout
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolumeCalculator @Inject constructor() {

    fun computeWeeklyVolumes(
        data: List<VolumeDataPoint>,
        start: LocalDate,
        end: LocalDate
    ): List<WeeklyVolume> {
        if (data.isEmpty()) return emptyList()

        return data
            .groupBy { it.workoutDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .map { (weekStart, weekData) ->
                val totalSets = weekData.sumOf { it.sets }
                val totalVolume = weekData.sumOf { it.weight * it.sets * it.reps }
                val trainingDays = weekData.map { it.workoutDate }.distinct().count()

                WeeklyVolume(
                    weekStart = weekStart,
                    totalSets = totalSets,
                    totalVolume = totalVolume,
                    trainingDays = trainingDays
                )
            }
            .filter { it.weekStart in start..end || it.weekStart >= start }
            .sortedBy { it.weekStart }
    }

    fun computeBodyPartStats(raw: List<BodyPartCount>): List<BodyPartStat> {
        if (raw.isEmpty()) return emptyList()

        val total = raw.sumOf { it.count }
        return raw
            .sortedByDescending { it.count }
            .map { stat ->
                BodyPartStat(
                    bodyPart = stat.bodyPart,
                    count = stat.count,
                    percentage = if (total > 0) stat.count.toFloat() / total else 0f
                )
            }
    }

    fun computeMonthlySummary(workouts: List<Workout>): MonthlySummary {
        var totalSets = 0
        var totalVolume = 0.0
        val trainingDays = mutableSetOf<LocalDate>()

        for (workout in workouts) {
            trainingDays.add(workout.date)
            for (exercise in workout.exercises) {
                val s = exercise.sets ?: 0
                val r = exercise.reps ?: 0
                val w = exercise.weight ?: 0.0
                totalSets += s
                totalVolume += w * s * r
            }
        }

        return MonthlySummary(
            totalSets = totalSets,
            totalVolume = totalVolume,
            trainingDays = trainingDays.size
        )
    }
}
