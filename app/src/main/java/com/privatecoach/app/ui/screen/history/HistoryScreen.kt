package com.privatecoach.app.ui.screen.history

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.core.model.toChinese
import com.privatecoach.app.ui.component.*
import com.privatecoach.app.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToWorkoutDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("记录", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = PcSpacing.md)) {
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            // Search bar
            Row(
                modifier = Modifier.fillMaxWidth().border(1.dp, PcDivider, PcShapes.small).padding(PcSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = PcTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearch(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("搜索动作名...", color = PcTextDisabled) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = PcAccentCopper
                    )
                )
            }
            Spacer(modifier = Modifier.height(PcSpacing.md))
            PcLineDivider()
            Spacer(modifier = Modifier.height(PcSpacing.sm))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { PcLoadingIndicator() }
            } else if (uiState.workouts.isEmpty()) {
                PcEmptyState(message = "还没有训练记录")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
                    items(uiState.workouts) { workout ->
                        WorkoutHistoryRow(workout = workout, onClick = { onNavigateToWorkoutDetail(workout.id) })
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryRow(workout: Workout, onClick: () -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日 EEEE")
    PcCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(workout.date.format(dateFormatter), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = PcAccentCopper)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (workout.type == WorkoutType.STRENGTH) StrengthTag() else CardioTag()
                    workout.bodyPart?.let { bp -> Spacer(modifier = Modifier.width(6.dp)); PcTag(text = bp) }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(workout.exercises.joinToString(" · ") { it.name }, style = MaterialTheme.typography.bodySmall, color = PcTextSecondary, maxLines = 1)
                workout.feeling?.let { feel ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("感受: ${feel.toChinese()}", style = MaterialTheme.typography.bodySmall, color = PcTextSecondary)
                }
            }
            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = PcTextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}
