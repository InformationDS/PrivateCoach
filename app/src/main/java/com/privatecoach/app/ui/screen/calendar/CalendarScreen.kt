package com.privatecoach.app.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.core.model.toChinese
import com.privatecoach.app.ui.component.PcCard
import com.privatecoach.app.ui.component.PcLineDivider
import com.privatecoach.app.ui.component.PcLoadingIndicator
import com.privatecoach.app.ui.component.PcStatValue
import com.privatecoach.app.ui.component.StrengthTag
import com.privatecoach.app.ui.component.CardioTag
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcAccentTeal
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcSpacing
import com.privatecoach.app.ui.theme.PcTextDisabled
import com.privatecoach.app.ui.theme.PcTextSecondary
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToWorkoutDetail: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日历",
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    Text(
                        text = if (uiState.isWeekView) "月" else "周",
                        color = PcTextSecondary,
                        modifier = Modifier
                            .clickable { viewModel.toggleViewMode() }
                            .padding(8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                PcLoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = PcSpacing.md)
            ) {
                // Monthly stats
                PcCard(modifier = Modifier.fillMaxWidth().padding(bottom = PcSpacing.md)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        PcStatValue(value = "${uiState.monthlyTrainingDays}", label = "训练天数", highlighted = true)
                        PcStatValue(value = "${uiState.monthlyTotalSets}", label = "总组数")
                        PcStatValue(value = "${uiState.workoutDates.size}", label = "有记录天数")
                    }
                }

                // Month/Week navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (uiState.isWeekView) viewModel.previousWeek() else viewModel.previousMonth()
                    }) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "上一页", tint = PcTextSecondary)
                    }
                    Text(
                        text = if (uiState.isWeekView) {
                            val weekEnd = uiState.currentWeekStart.plusDays(6)
                            "${uiState.currentWeekStart.format(DateTimeFormatter.ofPattern("M月d日"))} - ${weekEnd.format(DateTimeFormatter.ofPattern("M月d日"))}"
                        } else {
                            uiState.currentMonth.format(DateTimeFormatter.ofPattern("yyyy年 M月"))
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = {
                        if (uiState.isWeekView) viewModel.nextWeek() else viewModel.nextMonth()
                    }) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "下一页", tint = PcTextSecondary)
                    }
                }

                PcLineDivider()
                Spacer(modifier = Modifier.height(PcSpacing.sm))

                // Day-of-week headers
                val daysOfWeek = listOf("一", "二", "三", "四", "五", "六", "日")
                Row(modifier = Modifier.fillMaxWidth()) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = PcTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PcSpacing.sm))

                // Calendar grid
                val startDay = if (uiState.isWeekView) {
                    uiState.currentWeekStart
                } else {
                    uiState.currentMonth.atDay(1).with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                }
                val daysInGrid = if (uiState.isWeekView) 7 else {
                    (ChronoUnit.DAYS.between(startDay, uiState.currentMonth.atEndOfMonth()) + 1).toInt()
                        .let { ((it + 6) / 7) * 7 }
                }

                val today = LocalDate.now()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daysInGrid) { index ->
                        val date = startDay.plusDays(index.toLong())
                        val isCurrentMonth = date.month == uiState.currentMonth.month || uiState.isWeekView
                        val hasWorkout = date in uiState.workoutDates
                        val isToday = date == today
                        val isSelected = date == uiState.selectedDate

                        DayCell(
                            date = date,
                            isCurrentMonth = isCurrentMonth,
                            hasWorkout = hasWorkout,
                            isToday = isToday,
                            isSelected = isSelected,
                            onClick = { viewModel.selectDate(date) }
                        )
                    }
                }

                // Week view: day detail cards
                if (uiState.isWeekView) {
                    Spacer(modifier = Modifier.height(PcSpacing.md))
                    PcLineDivider()
                    Spacer(modifier = Modifier.height(PcSpacing.sm))

                    val weekDays = (0..6).map { uiState.currentWeekStart.plusDays(it.toLong()) }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(PcSpacing.sm)
                    ) {
                        items(weekDays) { dayDate ->
                            val workouts = uiState.weeklyWorkouts[dayDate] ?: emptyList()
                            val dateFormatter = DateTimeFormatter.ofPattern("M/d EEEE")
                            PcCard(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (workouts.isNotEmpty()) {
                                        // Select date to trigger bottom sheet
                                        viewModel.selectDate(dayDate)
                                    }
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        dayDate.format(dateFormatter),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (dayDate == today) PcAccentCopper else PcTextSecondary,
                                        fontWeight = if (dayDate == today) FontWeight.Medium else FontWeight.Normal,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    if (workouts.isEmpty()) {
                                        Text(
                                            "无训练记录",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PcTextDisabled
                                        )
                                    } else {
                                        Column(modifier = Modifier.weight(1f)) {
                                            workouts.forEach { workout ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (workout.type == com.privatecoach.app.core.model.WorkoutType.STRENGTH) {
                                                        StrengthTag()
                                                    } else {
                                                        CardioTag()
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        workout.exercises.joinToString(" · ") { it.name },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = PcTextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
            }
        }
    }

    // Day detail bottom sheet
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember(uiState.selectedDate) { mutableStateOf(uiState.selectedDate != null) }

    if (showSheet && uiState.selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = PcBackground,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(modifier = Modifier.padding(PcSpacing.md)) {
                Text(
                    text = uiState.selectedDate!!.format(DateTimeFormatter.ofPattern("M月d日 EEEE")),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(PcSpacing.md))

                val dayWorkouts = uiState.selectedDayWorkouts
                if (dayWorkouts.isEmpty()) {
                    Text(
                        text = "无训练记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = PcTextSecondary
                    )
                } else {
                    dayWorkouts.forEach { workout ->
                        PcCard(
                            modifier = Modifier.fillMaxWidth().padding(bottom = PcSpacing.sm)
                                .clickable { onNavigateToWorkoutDetail(workout.id) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (workout.type == com.privatecoach.app.core.model.WorkoutType.STRENGTH) {
                                    StrengthTag()
                                } else {
                                    CardioTag()
                                }
                                Spacer(modifier = Modifier.width(PcSpacing.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        workout.exercises.joinToString(" · ") { it.name },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PcTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    workout.exercises.firstOrNull()?.feeling?.let { feel ->
                                        Text(
                                            "感受: ${feel.toChinese()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PcTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(PcSpacing.lg))
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    hasWorkout: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.border(1.dp, PcAccentCopper, CircleShape)
                else if (isToday) Modifier.border(1.dp, PcDivider, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                color = if (!isCurrentMonth) PcTextSecondary.copy(alpha = 0.3f)
                        else if (isToday) PcAccentCopper
                        else MaterialTheme.colorScheme.onBackground,
                fontWeight = if (isToday || isSelected) FontWeight.Medium else FontWeight.Normal
            )
            if (hasWorkout) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(PcAccentCopper)
                )
            }
        }
    }
}
