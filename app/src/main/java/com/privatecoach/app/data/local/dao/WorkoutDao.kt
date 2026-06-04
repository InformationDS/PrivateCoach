package com.privatecoach.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.privatecoach.app.core.model.BodyPartCount
import com.privatecoach.app.core.model.ExerciseTrendPoint
import com.privatecoach.app.data.local.entity.WorkoutEntity
import com.privatecoach.app.core.model.TrainingFrequencyPoint
import com.privatecoach.app.core.model.VolumeDataPoint
import com.privatecoach.app.data.local.relation.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkoutDao {

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC, created_at DESC")
    fun getAllWorkouts(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkoutById(workoutId: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC, created_at DESC LIMIT :limit")
    fun getRecentWorkouts(limit: Int): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY created_at DESC LIMIT 1")
    suspend fun getWorkoutByDate(date: LocalDate): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutByIdOnce(workoutId: Long): WorkoutWithExercises?

    @Query("SELECT DISTINCT date FROM workouts WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getDistinctWorkoutDates(startDate: LocalDate, endDate: LocalDate): List<LocalDate>

    @Query("SELECT COUNT(DISTINCT date) FROM workouts WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTrainingDaysCount(startDate: LocalDate, endDate: LocalDate): Int

    @Transaction
    @Query("""
        SELECT DISTINCT w.* FROM workouts w
        INNER JOIN exercises e ON e.workout_id = w.id
        WHERE e.name LIKE '%' || :query || '%'
        ORDER BY w.date DESC
    """)
    fun searchWorkoutsByExerciseName(query: String): Flow<List<WorkoutWithExercises>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    @Query("""
        SELECT e.name, w.date, e.weight, e.sets, e.reps
        FROM exercises e
        INNER JOIN workouts w ON w.id = e.workout_id
        WHERE e.name = :exerciseName AND w.type = 'STRENGTH' AND e.weight IS NOT NULL
        ORDER BY w.date ASC
    """)
    suspend fun getExerciseTrendData(exerciseName: String): List<ExerciseTrendPoint>

    @Query("SELECT DISTINCT e.name FROM exercises e ORDER BY e.name")
    fun getAllExerciseNames(): Flow<List<String>>

    @Query("""
        SELECT w.body_part as bodyPart, COUNT(w.id) as count
        FROM workouts w
        WHERE w.date BETWEEN :startDate AND :endDate AND w.body_part IS NOT NULL
        GROUP BY w.body_part
    """)
    suspend fun getBodyPartDistribution(startDate: LocalDate, endDate: LocalDate): List<BodyPartCount>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC, created_at DESC")
    suspend fun getAllWorkoutsOnce(): List<WorkoutWithExercises>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC LIMIT 1")
    suspend fun getMostRecentWorkoutOnce(): WorkoutWithExercises?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntity>): List<Long>

    @Query("""
        SELECT w.date as workoutDate, e.weight, e.sets, e.reps
        FROM workouts w
        INNER JOIN exercises e ON e.workout_id = w.id
        WHERE w.date BETWEEN :startDate AND :endDate
          AND e.weight IS NOT NULL AND e.sets IS NOT NULL AND e.reps IS NOT NULL
        ORDER BY w.date ASC
    """)
    suspend fun getVolumeData(startDate: LocalDate, endDate: LocalDate): List<VolumeDataPoint>

    @Query("""
        SELECT w.date, COUNT(DISTINCT w.id) as sessions
        FROM workouts w
        WHERE w.date BETWEEN :startDate AND :endDate
        GROUP BY w.date
        ORDER BY w.date ASC
    """)
    suspend fun getTrainingFrequency(startDate: LocalDate, endDate: LocalDate): List<TrainingFrequencyPoint>

    @Query("""
        SELECT e.name, w.date, e.weight, e.sets, e.reps
        FROM exercises e
        INNER JOIN workouts w ON w.id = e.workout_id
        WHERE w.body_part = :bodyPart AND w.type = 'STRENGTH' AND e.weight IS NOT NULL
        ORDER BY w.date ASC
    """)
    suspend fun getBodyPartTrendData(bodyPart: String): List<ExerciseTrendPoint>
}
