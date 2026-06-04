package com.privatecoach.app.core.export

import android.content.Context
import android.net.Uri
import com.privatecoach.app.core.model.ExportContainer
import com.privatecoach.app.data.mapper.toExportTemplate
import com.privatecoach.app.data.mapper.toExportWorkout
import com.privatecoach.app.domain.repository.TemplateRepository
import com.privatecoach.app.domain.repository.WorkoutRepository
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExporter @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: TemplateRepository
) {
    suspend fun exportToJson(): ExportContainer {
        val workouts = workoutRepository.getAllWorkoutsOnce()
        val templates = templateRepository.getAllTemplatesOnce()
        return ExportContainer(
            version = 1,
            exportedAt = Instant.now().toString(),
            appVersion = "1.0.0",
            workouts = workouts.map { it.toExportWorkout() },
            templates = templates.map { it.toExportTemplate() }
        )
    }

    fun writeToUri(context: Context, uri: Uri, container: ExportContainer) {
        val json = Json { prettyPrint = true }.encodeToString(ExportContainer.serializer(), container)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(json.toByteArray(Charsets.UTF_8))
        }
    }
}
