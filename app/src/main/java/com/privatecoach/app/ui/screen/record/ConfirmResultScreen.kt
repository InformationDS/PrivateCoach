package com.privatecoach.app.ui.screen.record

import androidx.compose.foundation.clickable
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
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.ui.component.*
import com.privatecoach.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmResultScreen(
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: ConfirmResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) onSaveComplete()
    }

    val parsed = uiState.parsedResult
    if (parsed == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("没有待确认的训练数据", color = PcTextSecondary)
        }
        return
    }

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("确认训练", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = PcTextSecondary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveWorkout() },
                        enabled = !uiState.isSaving
                    ) {
                        Text("保存", color = if (!uiState.isSaving) PcAccentCopper else PcTextDisabled)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = PcSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PcSpacing.md)
        ) {
            item { Spacer(modifier = Modifier.height(PcSpacing.sm)) }

            // Training type & body part
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                    if (parsed.type == WorkoutType.STRENGTH) StrengthTag() else CardioTag()
                }
                Spacer(modifier = Modifier.height(PcSpacing.sm))
                PcTextField(
                    value = uiState.bodyPart,
                    onValueChange = { viewModel.setBodyPart(it) },
                    placeholder = "训练部位"
                )
            }

            // Feeling
            item {
                Text("感受", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                Spacer(modifier = Modifier.height(PcSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                    listOf("轻松", "良好", "一般", "疲劳").forEach { feel ->
                        val selected = uiState.feeling == feel
                        PcTag(
                            text = feel,
                            color = if (selected) PcAccentCopper else PcDivider,
                            modifier = Modifier.clickable { viewModel.setFeeling(if (selected) null else feel) }
                        )
                    }
                }
            }

            // Exercises header
            item {
                Text("动作列表", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                PcLineDivider()
            }

            // Editable exercises
            itemsIndexed(uiState.exercises) { index, exercise ->
                PcCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = PcAccentCopper)
                        Spacer(modifier = Modifier.width(PcSpacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            PcTextField(
                                value = exercise.name,
                                onValueChange = { viewModel.updateExerciseName(index, it) },
                                placeholder = "动作名"
                            )
                            Spacer(modifier = Modifier.height(PcSpacing.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                                PcTextField(
                                    value = exercise.weight?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "",
                                    onValueChange = { viewModel.updateExerciseWeight(index, it) },
                                    placeholder = "重量",
                                    modifier = Modifier.weight(1f)
                                )
                                PcTextField(
                                    value = exercise.sets?.toString() ?: "",
                                    onValueChange = { viewModel.updateExerciseSets(index, it) },
                                    placeholder = "组数",
                                    modifier = Modifier.weight(1f)
                                )
                                PcTextField(
                                    value = exercise.reps?.toString() ?: "",
                                    onValueChange = { viewModel.updateExerciseReps(index, it) },
                                    placeholder = "次数",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // AI Summary
            item {
                Spacer(modifier = Modifier.height(PcSpacing.sm))
                Text("AI 训练总结", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                PcLineDivider()
                Spacer(modifier = Modifier.height(PcSpacing.sm))
                PcCard {
                    Text(parsed.summaryMarkdown, style = MaterialTheme.typography.bodyMedium, color = PcTextSecondary)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
