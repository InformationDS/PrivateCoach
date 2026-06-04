package com.privatecoach.app.domain.repository

import com.privatecoach.app.core.model.BodyPartCount
import com.privatecoach.app.core.model.ExerciseTrendPoint
import com.privatecoach.app.core.model.TrainingFrequencyPoint
import com.privatecoach.app.core.model.VolumeDataPoint
import com.privatecoach.app.core.model.Workout
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    fun getWorkoutById(id: Long): Flow<Workout?>
    fun getWorkoutsByDateRange(start: LocalDate, end: LocalDate): Flow<List<Workout>>
    fun getRecentWorkouts(limit: Int): Flow<List<Workout>>
    fun searchWorkouts(query: String): Flow<List<Workout>>
    fun getAllExerciseNames(): Flow<List<String>>

    suspend fun getAllWorkoutsOnce(): List<Workout>
    suspend fun getMostRecentWorkoutOnce(): Workout?
    suspend fun createWorkout(workout: Workout): Long
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workoutId: Long)
    suspend fun insertWorkouts(workouts: List<Workout>)

    suspend fun getDistinctWorkoutDates(start: LocalDate, end: LocalDate): List<LocalDate>
    suspend fun getExerciseTrendData(exerciseName: String): List<ExerciseTrendPoint>
    suspend fun getBodyPartDistribution(start: LocalDate, end: LocalDate): List<BodyPartCount>
    suspend fun getVolumeData(start: LocalDate, end: LocalDate): List<VolumeDataPoint>
    suspend fun getTrainingFrequency(start: LocalDate, end: LocalDate): List<TrainingFrequencyPoint>
    suspend fun getTrainingDaysCount(start: LocalDate, end: LocalDate): Int
    suspend fun getWorkoutByIdOnce(id: Long): Workout?
}
