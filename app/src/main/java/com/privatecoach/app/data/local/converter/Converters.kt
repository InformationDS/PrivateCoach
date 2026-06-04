package com.privatecoach.app.data.local.converter

import androidx.room.TypeConverter
import com.privatecoach.app.core.model.InputMode
import com.privatecoach.app.core.model.WorkoutType
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter fun fromWorkoutType(value: WorkoutType): String = value.name
    @TypeConverter fun toWorkoutType(value: String): WorkoutType = WorkoutType.valueOf(value)
    @TypeConverter fun fromInputMode(value: InputMode): String = value.name
    @TypeConverter fun toInputMode(value: String): InputMode = InputMode.valueOf(value)
    @TypeConverter fun fromInstant(value: Instant): Long = value.toEpochMilli()
    @TypeConverter fun toInstant(value: Long): Instant = Instant.ofEpochMilli(value)
    @TypeConverter fun fromLocalDate(value: LocalDate): String = value.toString()
    @TypeConverter fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)
}
