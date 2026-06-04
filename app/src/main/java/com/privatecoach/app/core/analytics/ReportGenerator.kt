package com.privatecoach.app.core.analytics

import com.privatecoach.app.core.ai.AiApiService
import com.privatecoach.app.core.model.Report
import com.privatecoach.app.core.model.ReportInputData
import com.privatecoach.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportGenerator @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiApiService: AiApiService,
    private val trendAnalyzer: TrendAnalyzer,
    private val volumeCalculator: VolumeCalculator
) {

    suspend fun generate(
        periodName: String,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        previousPeriodStart: LocalDate,
        previousPeriodEnd: LocalDate,
        previousPeriodName: String
    ): Report {
        // Current period data
        val currentVolumeData = workoutRepository.getVolumeData(periodStart, periodEnd)
        val currentFreqData = workoutRepository.getTrainingFrequency(periodStart, periodEnd)
        val trainingDays = currentFreqData.size
        val workoutsInPeriod =
            workoutRepository.getWorkoutsByDateRange(periodStart, periodEnd).first()
        val totalExercises = workoutsInPeriod.sumOf { it.exercises.size }

        // Previous period data for comparison
        val previousVolumeData = workoutRepository.getVolumeData(previousPeriodStart, previousPeriodEnd)

        val currentVolumes = volumeCalculator.computeWeeklyVolumes(
            currentVolumeData, periodStart, periodEnd
        )
        val previousVolumes = volumeCalculator.computeWeeklyVolumes(
            previousVolumeData, previousPeriodStart, previousPeriodEnd
        )

        // Volume change percentage
        val currentTotalVolume = currentVolumes.sumOf { it.totalVolume }
        val previousTotalVolume = previousVolumes.sumOf { it.totalVolume }
        val volumeChange: Float? = if (previousTotalVolume > 0) {
            ((currentTotalVolume - previousTotalVolume) / previousTotalVolume * 100).toFloat()
        } else null

        // Body part distribution
        val bodyPartRaw = workoutRepository.getBodyPartDistribution(periodStart, periodEnd)
        val topBodyParts = volumeCalculator.computeBodyPartStats(bodyPartRaw).take(5)

        // Top progress exercises (by weight increase)
        val allExerciseNames = workoutRepository.getAllExerciseNames().first()

        val progressExercises = mutableListOf<Pair<String, Double>>()
        for (name in allExerciseNames.take(10)) {
            val trend = workoutRepository.getExerciseTrendData(name)
            val first = trend.firstOrNull { it.weight != null }
            val last = trend.lastOrNull { it.weight != null }
            if (first != null && last != null && (last.weight ?: 0.0) > (first.weight ?: 0.0)) {
                val improvement = (last.weight ?: 0.0) - (first.weight ?: 0.0)
                progressExercises.add(name to improvement)
            }
        }
        val topProgressExercises = progressExercises
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        // Generate AI report
        val reportInput = ReportInputData(
            periodName = periodName,
            trainingDays = trainingDays,
            totalExercises = totalExercises,
            volumeChange = volumeChange,
            topBodyParts = topBodyParts,
            previousPeriodName = previousPeriodName,
            progressExercises = topProgressExercises
        )

        val aiSummary = aiApiService.generateReport(reportInput).getOrNull()

        return Report(
            title = "${periodName}训练报告",
            period = periodName,
            trainingDays = trainingDays,
            totalExercises = totalExercises,
            volumeChange = volumeChange,
            topBodyParts = topBodyParts,
            topProgressExercises = topProgressExercises,
            aiSummary = aiSummary,
            generatedAt = Instant.now()
        )
    }
}
