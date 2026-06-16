package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.SessionContext
import com.privatecoach.app.core.model.StagnationAlert
import com.privatecoach.app.core.model.WorkoutSummary
import com.privatecoach.app.domain.repository.WorkoutRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the session initialization context.
 * Called once per cold-start to inject user training profile into AI system prompt.
 */
@Singleton
class SessionContextBuilder @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend fun build(): SessionContext {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val fourWeeksAgo = today.minusWeeks(4)
        val twelveWeeksAgo = today.minusWeeks(12)

        // Query training data
        val trainingDaysThisWeek = workoutRepository.getTrainingDaysCount(weekStart, today)
        val recentWorkout = workoutRepository.getMostRecentWorkoutOnce()
        val allWorkouts = workoutRepository.getAllWorkoutsOnce()
        val bodyPartsThisWeek = workoutRepository.getBodyPartDistribution(weekStart, today)
        val frequencyData = workoutRepository.getTrainingFrequency(fourWeeksAgo, today)
        val allExerciseNames = workoutRepository.getAllExerciseNames()

        // Compute body part coverage
        val coveredParts = bodyPartsThisWeek.mapNotNull { bp ->
            try { BodyPart.fromChinese(bp.bodyPart) } catch (_: Exception) { null }
        }.toSet()
        val allBodyParts = BodyPart.entries.filter { it != BodyPart.FULL_BODY }
        val uncoveredParts = allBodyParts.filter { it !in coveredParts }

        // Build recent workout summary
        val recentSummary = recentWorkout?.let { w ->
            WorkoutSummary(
                date = w.date,
                bodyPart = w.bodyPart,
                type = w.type,
                exerciseNames = w.exercises.map { it.name },
                daysAgo = ChronoUnit.DAYS.between(w.date, today).toInt()
            )
        }

        // Detect stagnating exercises (>4 weeks without weight increase)
        val stagnatingExercises = detectStagnation(allWorkouts, twelveWeeksAgo, today)

        // Compute weekly frequency (average over last 4 weeks)
        val weeklyFrequency = if (frequencyData.isNotEmpty()) {
            frequencyData.sumOf { it.sessions }.toDouble() / 4.0
        } else 0.0

        // Frequent exercises (top 10 by occurrence)
        val exerciseFrequency = mutableMapOf<String, Int>()
        allWorkouts.forEach { workout ->
            workout.exercises.forEach { ex ->
                exerciseFrequency[ex.name] = (exerciseFrequency[ex.name] ?: 0) + 1
            }
        }
        val frequentExercises = exerciseFrequency.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }

        return SessionContext(
            trainingDaysThisWeek = trainingDaysThisWeek,
            recentWorkoutSummary = recentSummary,
            coveredBodyParts = coveredParts,
            uncoveredBodyParts = uncoveredParts,
            stagnatingExercises = stagnatingExercises,
            frequentExercises = frequentExercises,
            weeklyFrequency = weeklyFrequency,
            totalWorkoutCount = allWorkouts.size
        )
    }

    private fun detectStagnation(
        allWorkouts: List<com.privatecoach.app.core.model.Workout>,
        since: LocalDate,
        today: LocalDate
    ): List<StagnationAlert> {
        // Group exercises by name, track max weight over time
        val exerciseHistory = mutableMapOf<String, MutableList<Pair<LocalDate, Double>>>()

        allWorkouts.forEach { workout ->
            workout.exercises.forEach { ex ->
                if (ex.weight != null && ex.weight > 0 && !workout.date.isBefore(since)) {
                    exerciseHistory.getOrPut(ex.name) { mutableListOf() }
                        .add(workout.date to ex.weight)
                }
            }
        }

        val alerts = mutableListOf<StagnationAlert>()
        exerciseHistory.forEach { (name, history) ->
            history.sortBy { it.first }
            if (history.size >= 3) {
                val maxWeight = history.maxOf { it.second }
                val firstDate = history.first().first
                val lastDate = history.last().first
                val weeksBetween = ChronoUnit.WEEKS.between(firstDate, lastDate)

                // If max weight hasn't changed in 4+ weeks and has enough history
                if (weeksBetween >= 4) {
                    val recentMax = history.takeLast(3).maxOf { it.second }
                    val earlierMax = history.dropLast(3).maxOfOrNull { it.second }
                    if (earlierMax != null && recentMax <= earlierMax) {
                        alerts.add(
                            StagnationAlert(
                                exerciseName = name,
                                currentWeight = recentMax,
                                weeksStagnant = weeksBetween.toInt(),
                                lastProgressDate = history.lastOrNull { it.second > earlierMax }?.first
                            )
                        )
                    }
                }
            }
        }

        return alerts.sortedByDescending { it.weeksStagnant }.take(3)
    }
}
