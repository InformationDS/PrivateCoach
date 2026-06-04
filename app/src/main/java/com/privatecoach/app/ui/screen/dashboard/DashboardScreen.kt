package com.privatecoach.app.ui.screen.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.core.model.toChinese
import com.privatecoach.app.ui.component.CardioTag
import com.privatecoach.app.ui.component.PcCard
import com.privatecoach.app.ui.component.PcEmptyState
import com.privatecoach.app.ui.component.PcLineDivider
import com.privatecoach.app.ui.component.PcLoadingIndicator
import com.privatecoach.app.ui.component.PcStatValue
import com.privatecoach.app.ui.component.PcTag
import com.privatecoach.app.ui.component.StrengthTag
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcSpacing
import com.privatecoach.app.ui.theme.PcTextSecondary
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToRecord: () -> Unit,
    onNavigateToWorkoutDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PrivateCoach",
                        fontWeight = FontWeight.Light,
                        color = PcAccentCopper
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Outlined.KeyboardArrowRight,
                            contentDescription = "设置",
                            tint = PcTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        },
        floatingActionButton = {
            RecordFab(onClick = onNavigateToRecord)
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                PcLoadingIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = PcSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PcSpacing.md)
            ) {
                // Stats card
                item {
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    TodayStatusCard(
                        trainingDays = uiState.trainingDaysThisWeek,
                        lastWorkoutDaysAgo = uiState.lastWorkoutDaysAgo,
                        hasWorkoutToday = uiState.hasWorkoutToday,
                        today = uiState.today
                    )
                }

                // Recent workouts header
                item {
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    Text(
                        text = "最近训练",
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        color = PcTextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(PcSpacing.xs))
                    PcLineDivider()
                }

                if (uiState.recentWorkouts.isEmpty()) {
                    item {
                        PcEmptyState(message = "还没有训练记录\n点击右下角开始记录")
                    }
                } else {
                    items(uiState.recentWorkouts) { workout ->
                        WorkoutPreviewCard(
                            workout = workout,
                            onClick = { onNavigateToWorkoutDetail(workout.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
}

@Composable
private fun TodayStatusCard(
    trainingDays: Int,
    lastWorkoutDaysAgo: Int?,
    hasWorkoutToday: Boolean,
    today: java.time.LocalDate
) {
    PcCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PcStatValue(
                value = "$trainingDays",
                label = "本周训练天数",
                highlighted = trainingDays > 0
            )
            PcStatValue(
                value = lastWorkoutDaysAgo?.toString() ?: "-",
                label = "天前训练"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (hasWorkoutToday) "已练" else "未练",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (hasWorkoutToday) PcAccentCopper else PcTextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "今日",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = PcTextSecondary
                )
            }
        }
    }
}

@Composable
private fun WorkoutPreviewCard(
    workout: Workout,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")
    PcCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = workout.date.format(dateFormatter),
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = PcAccentCopper
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (workout.type == WorkoutType.STRENGTH) StrengthTag() else CardioTag()
                workout.bodyPart?.let { bp ->
                    Spacer(modifier = Modifier.width(8.dp))
                    PcTag(text = bp)
                }
            }
            Icon(
                Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = PcTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = workout.exercises.joinToString(" · ") { it.name },
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = PcTextSecondary,
            maxLines = 1
        )
        workout.feeling?.let { feel ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "感受: ${feel.toChinese()}",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = PcTextSecondary
            )
        }
    }
}

@Composable
private fun RecordFab(onClick: () -> Unit) {
    androidx.compose.material3.FloatingActionButton(
        onClick = onClick,
        containerColor = PcAccentCopper,
        contentColor = PcBackground,
        shape = PcShapes.medium,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription = "录音",
            modifier = Modifier.size(24.dp)
        )
    }
}
