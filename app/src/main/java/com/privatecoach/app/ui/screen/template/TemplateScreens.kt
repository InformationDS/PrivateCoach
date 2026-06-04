package com.privatecoach.app.ui.screen.template

import androidx.compose.foundation.border
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
fun TemplateListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long?) -> Unit,
    viewModel: TemplateListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("训练模板", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = PcTextSecondary)
                    }
                },
                actions = {
                    TextButton(onClick = { onNavigateToEdit(null) }) {
                        Text("新建", color = PcAccentCopper)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { PcLoadingIndicator() }
        } else if (uiState.templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                PcEmptyState(message = "还没有训练模板\n从训练详情页保存您的第一个模板")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = PcSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PcSpacing.sm)
            ) {
                item { Spacer(modifier = Modifier.height(PcSpacing.sm)) }
                itemsIndexed(uiState.templates) { _, template ->
                    PcCard(modifier = Modifier.fillMaxWidth().clickable { onNavigateToEdit(template.id) }) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(template.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                                    if (template.type == WorkoutType.STRENGTH) StrengthTag() else CardioTag()
                                    template.bodyPart?.let { PcTag(text = it.chineseName) }
                                }
                                Text("${template.exercises.size} 个动作", style = MaterialTheme.typography.bodySmall, color = PcTextSecondary, modifier = Modifier.padding(top = 4.dp))
                            }
                            IconButton(onClick = { viewModel.deleteTemplate(template.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = PcTextDisabled, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: TemplateEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) onNavigateBack()
    }

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("编辑模板", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = PcTextSecondary)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save() }, enabled = !uiState.isSaving && uiState.name.isNotBlank()) {
                        Text("保存", color = if (uiState.name.isNotBlank()) PcAccentCopper else PcTextDisabled)
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

            item { PcTextField(value = uiState.name, onValueChange = { viewModel.setName(it) }, placeholder = "模板名称（如：推胸日）") }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                    listOf(WorkoutType.STRENGTH to "力量训练", WorkoutType.CARDIO to "有氧训练").forEach { (type, label) ->
                        val selected = uiState.type == type
                        Box(
                            modifier = Modifier.weight(1f).border(1.dp, if (selected) PcAccentCopper else PcDivider, PcShapes.small).clickable { viewModel.setType(type) }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(label, color = if (selected) PcAccentCopper else PcTextSecondary, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal) }
                    }
                }
            }

            // Body part chip selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PcSpacing.xs)
                ) {
                    com.privatecoach.app.core.model.BodyPart.selectableList.forEach { bp ->
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

            item { Text("动作列表", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary); PcLineDivider() }

            itemsIndexed(uiState.exercises) { index, exercise ->
                PcCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = PcAccentCopper)
                        Spacer(modifier = Modifier.width(PcSpacing.sm))
                        PcTextField(value = exercise.name, onValueChange = { viewModel.updateExerciseName(index, it) }, placeholder = "动作名", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(PcSpacing.xs))
                        PcTag(
                            text = exercise.weightUnit,
                            color = PcDivider,
                            modifier = Modifier.clickable { viewModel.updateExerciseUnit(index, if (exercise.weightUnit == "kg") "lb" else "kg") }
                        )
                        IconButton(onClick = { viewModel.removeExercise(index) }, enabled = uiState.exercises.size > 1) {
                            Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = PcTextDisabled, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

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
