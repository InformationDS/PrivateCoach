package com.privatecoach.app.ui.screen.conversation.components.bubbles

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.Feeling
import com.privatecoach.app.core.model.ParsedExercise
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcAccentTeal
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcFeelingGood
import com.privatecoach.app.ui.theme.PcFeelingTired
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcSurface
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ConfirmCard(
    parsedResult: AiParsedResult,
    sourceText: String,
    isAppendMode: Boolean,
    onConfirm: (AiParsedResult) -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onAppend: () -> Unit,
    onOverwrite: () -> Unit,
    onAddExercise: ((String) -> Unit)? = null
) {
    val typeLabel = when (parsedResult.type) {
        WorkoutType.STRENGTH -> "🏋️"
        WorkoutType.CARDIO -> "🏃"
    }
    val bodyPartLabel = parsedResult.bodyPart?.chineseName ?: "综合训练"
    val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, PcAccentCopper, PcShapes.small)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$typeLabel $bodyPartLabel",
                color = PcAccentCopper,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = dateLabel,
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Source text (what was heard)
        Text(
            text = "「$sourceText」",
            color = PcTextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = PcDivider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Exercises list
        if (parsedResult.type == WorkoutType.STRENGTH) {
            parsedResult.exercises.forEach { exercise ->
                ExerciseRow(
                    exercise = exercise,
                    onEditExercise = { /* inline edit handled via ViewModel */ }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Cardio detail
        if (parsedResult.type == WorkoutType.CARDIO && parsedResult.cardioDetail != null) {
            CardioDetailRow(parsedResult.cardioDetail!!.let {
                val parts = mutableListOf<String>()
                if (it.duration != null) parts.add("${it.duration / 60}分钟")
                if (it.distance != null) parts.add("${it.distance}km")
                if (it.avgHeartRate != null) parts.add("心率${it.avgHeartRate}")
                parts.joinToString(" · ")
            })
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Feeling selector
        FeelRow(feeling = parsedResult.exercises.firstOrNull()?.feeling)

        Spacer(modifier = Modifier.height(8.dp))

        // Add exercise button
        TextButton(onClick = { onAddExercise?.invoke("") }) {
            Text("+ 添加动作", color = PcTextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = PcDivider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        if (isAppendMode) {
            // Existing workout — show append/overwrite prompt
            Text(
                text = "今天已有一条训练记录",
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onAppend) {
                    Text(
                        "📎 追加到今天的训练",
                        color = PcAccentCopper,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onOverwrite) {
                    Text(
                        "🔄 覆盖今天的记录",
                        color = PcFeelingTired,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCancel) {
                    Text("取消", color = PcTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            // New record — standard confirm/edit/cancel
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { onConfirm(parsedResult) }) {
                    Text(
                        "✅ 确认保存",
                        color = PcAccentCopper,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onEdit) {
                    Text("✏️ 编辑", color = PcTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onCancel) {
                    Text("🗑️ 取消", color = PcTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: ParsedExercise,
    onEditExercise: () -> Unit
) {
    val weightText = if (exercise.weight != null) "${String.format("%.0f", exercise.weight)}${exercise.weightUnit}" else "--kg"
    val setsText = if (exercise.sets != null) "${exercise.sets}组" else "--组"
    val repsText = if (exercise.reps != null) "${exercise.reps}次" else "--次"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${exercise.name}    $weightText ×$setsText ×$repsText",
            color = PcTextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "✏️",
            color = PcTextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .clickable { onEditExercise() }
                .padding(4.dp)
        )
    }
}

@Composable
private fun CardioDetailRow(detail: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🏃 $detail",
            color = PcAccentTeal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FeelRow(feeling: Feeling?) {
    var expanded by remember { mutableStateOf(false) }
    val currentFeel = feeling ?: Feeling.GOOD
    val feelLabel = when (currentFeel) {
        Feeling.EASY -> "轻松"
        Feeling.GOOD -> "良好"
        Feeling.NORMAL -> "一般"
        Feeling.TIRED -> "疲劳"
    }
    val feelColor = when (currentFeel) {
        Feeling.EASY -> PcFeelingGood
        Feeling.GOOD -> PcFeelingGood
        Feeling.NORMAL -> PcAccentCopper
        Feeling.TIRED -> PcFeelingTired
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "😊 感受：",
            color = PcTextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = feelLabel,
            color = feelColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Feeling.entries.forEach { f ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (f) {
                                Feeling.EASY -> "轻松"
                                Feeling.GOOD -> "良好"
                                Feeling.NORMAL -> "一般"
                                Feeling.TIRED -> "疲劳"
                            }
                        )
                    },
                    onClick = { expanded = false /* feeling change handled by ViewModel */ }
                )
            }
        }
    }
}
