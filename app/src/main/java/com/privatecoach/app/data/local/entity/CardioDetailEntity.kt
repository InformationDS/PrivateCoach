package com.privatecoach.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cardio_details",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exercise_id"], unique = true)]
)
data class CardioDetailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int,
    @ColumnInfo(name = "distance_km") val distanceKm: Double? = null,
    @ColumnInfo(name = "avg_heart_rate") val avgHeartRate: Int? = null,
    @ColumnInfo(name = "calories") val calories: Int? = null,
    @ColumnInfo(name = "cardio_type") val cardioType: String? = null
)
