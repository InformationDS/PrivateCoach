package com.privatecoach.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.privatecoach.app.core.model.InputMode
import com.privatecoach.app.core.model.WorkoutType
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "workouts",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["sync_id"], unique = true),
        Index(value = ["type"]),
        Index(value = ["template_id"])
    ]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "sync_id") val syncId: String? = null,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "type") val type: WorkoutType,
    @ColumnInfo(name = "body_part") val bodyPart: String? = null,
    /** Retained only for safe v1 migration; domain code uses exercise-level feeling. */
    @ColumnInfo(name = "feeling") val legacyFeeling: String? = null,
    @ColumnInfo(name = "ai_summary") val aiSummary: String? = null,
    @ColumnInfo(name = "raw_transcript") val rawTranscript: String? = null,
    @ColumnInfo(name = "audio_file_path") val audioFilePath: String? = null,
    @ColumnInfo(name = "input_mode") val inputMode: InputMode,
    @ColumnInfo(name = "template_id") val templateId: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)
