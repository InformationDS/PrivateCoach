package com.privatecoach.app.ui.screen.record

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.ui.component.*
import com.privatecoach.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: ManualEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) onSaveComplete()
    }

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("手动录入", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = PcTextSecondary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = !uiState.isSaving && uiState.exercises.any { it.name.isNotBlank() }
                    ) {
                        Text("保存", color = if (uiState.exercises.any { it.name.isNotBlank() }) PcAccentCopper else PcTextDisabled)
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
            // Workout type selector
            item {
                Spacer(modifier = Modifier.height(PcSpacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                    listOf(WorkoutType.STRENGTH to "力量训练", WorkoutType.CARDIO to "有氧训练").forEach { (type, label) ->
                        val selected = uiState.workoutType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (selected) PcAccentCopper else PcDivider, PcShapes.small)
                                .clickable { viewModel.setType(type) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (selected) PcAccentCopper else PcTextSecondary, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
                        }
                    }
                }
            }

            // Template picker
            item {
                if (uiState.templates.isNotEmpty()) {
                    Text("选择模板", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                        PcTag(text = "不使用", color = if (uiState.selectedTemplateId == null) PcAccentCopper else PcDivider, modifier = Modifier.clickable { viewModel.applyTemplate(null) })
                        uiState.templates.forEach { tmpl ->
                            PcTag(
                                text = tmpl.name,
                                color = if (uiState.selectedTemplateId == tmpl.id) PcAccentCopper else PcDivider,
                                modifier = Modifier.clickable { viewModel.applyTemplate(tmpl.id) }
                            )
                        }
                    }
                }
            }

            // Body part chip selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PcSpacing.xs)
                ) {
                    BodyPart.selectableList.forEach { bp ->
                        val selected = uiState.bodyPart == bp
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (selected) PcAccentCopper else PcDivider, PcShapes.extraSmall)
                                .clickable { viewModel.setBodyPart(if (selected) null else bp) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                bp.chineseName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) PcAccentCopper else PcTextSecondary
                            )
                        }
                    }
                }
            }

            // Section header
            item {
                Text("动作列表", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                PcLineDivider()
            }

            // Exercise form rows
            itemsIndexed(uiState.exercises) { index, exercise ->
                PcCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                        Spacer(modifier = Modifier.width(PcSpacing.xs))
                        IconButton(onClick = { viewModel.removeExercise(index) }, enabled = uiState.exercises.size > 1) {
                            Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = PcTextDisabled, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    PcTextField(
                        value = exercise.name,
                        onValueChange = { viewModel.updateExerciseName(index, it) },
                        placeholder = "动作名（如：平板卧推）"
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
                    Spacer(modifier = Modifier.height(4.dp))
                    // Per-exercise feeling
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf("轻松" to "EASY", "良好" to "GOOD", "一般" to "NORMAL", "疲劳" to "TIRED").forEach { (label, key) ->
                            val selected = exercise.feeling?.name == key
                            Box(
                                modifier = Modifier
                                    .border(1.dp, if (selected) PcAccentCopper else PcDivider, PcShapes.extraSmall)
                                    .clickable { viewModel.setExerciseFeeling(index, if (selected) null else label) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) PcAccentCopper else PcTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Add exercise button
            item {
                TextButton(onClick = { viewModel.addExercise() }) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = PcAccentCopper, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加动作", color = PcAccentCopper)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
