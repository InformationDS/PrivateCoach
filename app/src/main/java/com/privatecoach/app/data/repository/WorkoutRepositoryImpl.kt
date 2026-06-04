package com.privatecoach.app.data.repository

import com.privatecoach.app.core.model.BodyPartCount
import com.privatecoach.app.core.model.ExerciseTrendPoint
import com.privatecoach.app.core.model.TrainingFrequencyPoint
import com.privatecoach.app.core.model.VolumeDataPoint
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.data.local.dao.CardioDetailDao
import com.privatecoach.app.data.local.dao.ExerciseDao
import com.privatecoach.app.data.local.dao.WorkoutDao
import com.privatecoach.app.data.mapper.toDomain
import com.privatecoach.app.data.mapper.toEntity
import com.privatecoach.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val cardioDetailDao: CardioDetailDao
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<Workout>> =
        workoutDao.getAllWorkouts().map { list -> list.map { it.toDomain() } }

    override fun getWorkoutById(id: Long): Flow<Workout?> =
        workoutDao.getWorkoutById(id).map { it?.toDomain() }

    override fun getWorkoutsByDateRange(start: LocalDate, end: LocalDate): Flow<List<Workout>> =
        workoutDao.getWorkoutsByDateRange(start, end).map { list -> list.map { it.toDomain() } }

    override fun getRecentWorkouts(limit: Int): Flow<List<Workout>> =
        workoutDao.getRecentWorkouts(limit).map { list -> list.map { it.toDomain() } }

    override fun searchWorkouts(query: String): Flow<List<Workout>> =
        workoutDao.searchWorkoutsByExerciseName(query).map { list -> list.map { it.toDomain() } }

    override fun getAllExerciseNames(): Flow<List<String>> =
        workoutDao.getAllExerciseNames()

    override suspend fun createWorkout(workout: Workout): Long {
        val workoutEntity = workout.toEntity()
        val workoutId = workoutDao.insertWorkout(workoutEntity)
        workout.exercises.forEachIndexed { index, exercise ->
            val exEntity = exercise.toEntity(workoutId).copy(sortOrder = index)
            val exId = exerciseDao.insertAll(listOf(exEntity)).first()
            exercise.cardioDetail?.let { cardio ->
                cardioDetailDao.insert(cardio.toEntity().copy(exerciseId = exId))
            }
        }
        return workoutId
    }

    override suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout.toEntity())
        exerciseDao.deleteByWorkoutId(workout.id)
        workout.exercises.forEachIndexed { index, exercise ->
            val exEntity = exercise.toEntity(workout.id).copy(sortOrder = index)
            val exId = exerciseDao.insertAll(listOf(exEntity)).first()
            exercise.cardioDetail?.let { cardio ->
                cardioDetailDao.insert(cardio.toEntity().copy(exerciseId = exId))
            }
        }
    }

    override suspend fun deleteWorkout(workoutId: Long) {
        workoutDao.deleteWorkout(workoutId)
    }

    override suspend fun getDistinctWorkoutDates(start: LocalDate, end: LocalDate): List<LocalDate> =
        workoutDao.getDistinctWorkoutDates(start, end)

    override suspend fun getExerciseTrendData(exerciseName: String): List<ExerciseTrendPoint> =
        workoutDao.getExerciseTrendData(exerciseName)

    override suspend fun getBodyPartDistribution(start: LocalDate, end: LocalDate): List<BodyPartCount> =
        workoutDao.getBodyPartDistribution(start, end)

    override suspend fun getTrainingDaysCount(start: LocalDate, end: LocalDate): Int =
        workoutDao.getTrainingDaysCount(start, end)

    override suspend fun getVolumeData(start: LocalDate, end: LocalDate): List<VolumeDataPoint> =
        workoutDao.getVolumeData(start, end)

    override suspend fun getTrainingFrequency(start: LocalDate, end: LocalDate): List<TrainingFrequencyPoint> =
        workoutDao.getTrainingFrequency(start, end)

    override suspend fun getAllWorkoutsOnce(): List<Workout> =
        workoutDao.getAllWorkoutsOnce().map { it.toDomain() }

    override suspend fun getMostRecentWorkoutOnce(): Workout? =
        workoutDao.getMostRecentWorkoutOnce()?.toDomain()

    override suspend fun insertWorkouts(workouts: List<Workout>) {
        workouts.forEach { createWorkout(it) }
    }

    override suspend fun getWorkoutByIdOnce(id: Long): Workout? =
        workoutDao.getWorkoutByIdOnce(id)?.toDomain()
}
