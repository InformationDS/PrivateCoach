package com.privatecoach.app.ui.screen.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.Exercise
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.core.model.toChinese
import com.privatecoach.app.ui.component.*
import com.privatecoach.app.ui.theme.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.deleted) { if (uiState.deleted) onNavigateBack() }

    if (uiState.showDeleteDialog) {
        PcConfirmDialog(
            title = "删除训练记录",
            message = "确定要删除这条训练记录吗？此操作不可撤销。",
            confirmText = "删除",
            onConfirm = { viewModel.deleteWorkout() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("训练详情", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = PcTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { PcLoadingIndicator() }
        } else {
            val workout = uiState.workout ?: return@Scaffold

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = PcSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PcSpacing.md)
            ) {
                // Header
                item {
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    Text(
                        workout.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                        if (workout.type == WorkoutType.STRENGTH) StrengthTag() else CardioTag()
                        workout.bodyPart?.let { bp -> PcTag(text = bp.chineseName) }
                    }
                    Text("录入方式: ${when(workout.inputMode) { com.privatecoach.app.core.model.InputMode.VOICE -> "🎤 语音"; com.privatecoach.app.core.model.InputMode.TEXT -> "📝 文字"; com.privatecoach.app.core.model.InputMode.MANUAL -> "✍️ 手动" }}", style = MaterialTheme.typography.bodySmall, color = PcTextSecondary)
                }

                // Exercise list
                item {
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    Text("动作列表", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                    PcLineDivider()
                }

                itemsIndexed(workout.exercises) { index, exercise ->
                    ExerciseDetailRow(exercise = exercise, index = index + 1)
                }

                // AI summary
                workout.aiSummary?.let { summary ->
                    item {
                        Spacer(modifier = Modifier.height(PcSpacing.md))
                        Text("AI 训练总结", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                        PcLineDivider()
                        Spacer(modifier = Modifier.height(PcSpacing.sm))
                        PcCard {
                            Text(summary, style = MaterialTheme.typography.bodyMedium, color = PcTextSecondary)
                        }
                    }
                }

                // Actions
                item {
                    Spacer(modifier = Modifier.height(PcSpacing.lg))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OutlinedButton(onClick = { viewModel.saveAsTemplate() }, colors = ButtonDefaults.outlinedButtonColors(contentColor = PcAccentCopper)) {
                            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("保存为模板")
                        }
                        OutlinedButton(onClick = { viewModel.showDeleteDialog() }, colors = ButtonDefaults.outlinedButtonColors(contentColor = PcFeelingTired)) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("删除")
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ExerciseDetailRow(exercise: Exercise, index: Int) {
    PcCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$index.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = PcAccentCopper)
            Spacer(modifier = Modifier.width(PcSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(exercise.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.weight(1f))
                    exercise.feeling?.let { feel ->
                        Text("${feel.toChinese()}", style = MaterialTheme.typography.labelSmall, color = PcAccentCopper)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(PcSpacing.md)) {
                    exercise.weight?.let { w ->
                        Text("${if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()} ${exercise.weightUnit}", style = MaterialTheme.typography.bodySmall, color = PcTextSecondary)
                    }
                    exercise.sets?.let { s ->
                        exercise.reps?.let { r ->
                            Text("${s}组 × ${r}次", style = MaterialTheme.typography.bodySmall, color = PcTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
