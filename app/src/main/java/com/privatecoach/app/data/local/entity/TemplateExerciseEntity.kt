package com.privatecoach.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TrainingTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["template_id"])]
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "template_id") val templateId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "weight_unit") val weightUnit: String = "kg",
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "notes") val notes: String? = null
)
