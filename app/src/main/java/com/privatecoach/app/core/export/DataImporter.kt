package com.privatecoach.app.core.export

import android.content.Context
import android.net.Uri
import com.privatecoach.app.core.model.ExportContainer
import com.privatecoach.app.core.model.ImportResult
import com.privatecoach.app.data.mapper.toTrainingTemplate
import com.privatecoach.app.data.mapper.toWorkout
import com.privatecoach.app.domain.repository.TemplateRepository
import com.privatecoach.app.domain.repository.WorkoutRepository
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataImporter @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: TemplateRepository
) {
    fun readFromUri(context: Context, uri: Uri): ExportContainer {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IOException("无法读取文件")
        return Json { ignoreUnknownKeys = true }.decodeFromString(ExportContainer.serializer(), jsonString)
    }

    suspend fun importData(container: ExportContainer): ImportResult {
        val errors = mutableListOf<String>()
        var workoutsImported = 0
        var templatesImported = 0

        // Import templates first (workouts may reference them)
        container.templates.forEach { exportTemplate ->
            try {
                val template = exportTemplate.toTrainingTemplate()
                templateRepository.saveTemplate(template)
                templatesImported++
            } catch (e: Exception) {
                errors.add("导入模板 ${exportTemplate.name} 失败: ${e.message}")
            }
        }

        // Import workouts
        container.workouts.forEach { exportWorkout ->
            try {
                val workout = exportWorkout.toWorkout()
                workoutRepository.createWorkout(workout)
                workoutsImported++
            } catch (e: Exception) {
                errors.add("导入训练记录失败: ${e.message}")
            }
        }

        return ImportResult(
            workoutsImported = workoutsImported,
            templatesImported = templatesImported,
            errors = errors
        )
    }
}
