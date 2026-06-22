package com.privatecoach.app.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import com.privatecoach.app.MainActivity
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class TrainingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.workoutRepository()

        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val trainingDays = repository.getTrainingDaysCount(weekStart, weekEnd)
        val hasWorkoutToday = repository.getDistinctWorkoutDates(today, today).isNotEmpty()
        val lastWorkout = repository.getMostRecentWorkoutOnce()
        val daysSinceLastWorkout = lastWorkout?.let {
            ChronoUnit.DAYS.between(it.date, today).toInt()
        }

        provideContent {
            GlanceTheme {
                WidgetContent(
                    context = context,
                    trainingDays = trainingDays,
                    hasWorkoutToday = hasWorkoutToday,
                    daysSinceLastWorkout = daysSinceLastWorkout
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context,
        trainingDays: Int,
        hasWorkoutToday: Boolean,
        daysSinceLastWorkout: Int?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "record")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Border effect: outer Box copper, inner Box navy
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(CopperBorder)
                .cornerRadius(4.dp)
                .clickable(actionStartActivity(intent))
                .padding(1.dp)
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(NavyBackground)
                    .cornerRadius(3.dp)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(com.privatecoach.app.R.drawable.ic_widget_dumbbell),
                        contentDescription = "训练",
                        modifier = GlanceModifier.size(28.dp),
                        colorFilter = ColorFilter.tint(CopperAccent)
                    )
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "本周训练 $trainingDays 天",
                            style = TextStyle(
                                color = CopperAccent,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = if (daysSinceLastWorkout != null) {
                                "上次训练 · ${daysSinceLastWorkout}天前"
                            } else {
                                "上次训练 · 暂无"
                            },
                            style = TextStyle(
                                color = SecondaryText,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        Text(
                            text = if (hasWorkoutToday) "今日 已训练 ✓" else "今日 未训练",
                            style = TextStyle(
                                color = if (hasWorkoutToday) CopperAccent else SecondaryText,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun workoutRepository(): WorkoutRepository
}

// Glance colors
private val NavyBackground = ColorProvider(Color(0xFF0F172A))
private val CopperAccent = ColorProvider(Color(0xFFD4A574))
private val CopperBorder = ColorProvider(Color(0xFFD4A574))
private val SecondaryText = ColorProvider(Color(0xFF8E9BAE))
