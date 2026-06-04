package com.privatecoach.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workout_id"]),
        Index(value = ["name"])
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "weight") val weight: Double? = null,
    @ColumnInfo(name = "weight_unit") val weightUnit: String = "kg",
    @ColumnInfo(name = "sets") val sets: Int? = null,
    @ColumnInfo(name = "reps") val reps: Int? = null,
    @ColumnInfo(name = "duration") val duration: Int? = null,
    @ColumnInfo(name = "distance") val distance: Double? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "notes") val notes: String? = null
)
