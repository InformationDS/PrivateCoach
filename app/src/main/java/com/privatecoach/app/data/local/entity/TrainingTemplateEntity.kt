package com.privatecoach.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.privatecoach.app.core.model.WorkoutType
import java.time.Instant

@Entity(
    tableName = "training_templates",
    indices = [Index(value = ["name"])]
)
data class TrainingTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: WorkoutType,
    @ColumnInfo(name = "body_part") val bodyPart: String? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)
