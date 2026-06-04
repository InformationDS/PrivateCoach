package com.privatecoach.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.privatecoach.app.data.local.entity.CardioDetailEntity
import com.privatecoach.app.data.local.entity.ExerciseEntity
import com.privatecoach.app.data.local.entity.TemplateExerciseEntity
import com.privatecoach.app.data.local.entity.TrainingTemplateEntity
import com.privatecoach.app.data.local.entity.WorkoutEntity

data class ExerciseWithCardioDetail(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exercise_id",
        entity = CardioDetailEntity::class
    )
    val cardioDetail: CardioDetailEntity?
)

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workout_id",
        entity = ExerciseEntity::class
    )
    val exercises: List<ExerciseWithCardioDetail>
)

data class TemplateWithExercises(
    @Embedded val template: TrainingTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "template_id",
        entity = TemplateExerciseEntity::class
    )
    val exercises: List<TemplateExerciseEntity>
)
