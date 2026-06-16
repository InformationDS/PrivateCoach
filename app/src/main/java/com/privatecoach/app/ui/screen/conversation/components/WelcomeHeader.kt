package com.privatecoach.app.ui.screen.conversation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.SessionContext
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun WelcomeHeader(
    sessionContext: SessionContext?,
    modifier: Modifier = Modifier
) {
    if (sessionContext == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, PcDivider, PcShapes.small)
            .padding(16.dp)
    ) {
        // Greeting
        Text(
            text = "👋 欢迎回来",
            color = PcAccentCopper,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Weekly stats
        if (sessionContext.trainingDaysThisWeek > 0) {
            Text(
                text = "本周已训练 ${sessionContext.trainingDaysThisWeek} 次" +
                    if (sessionContext.weeklyFrequency > 0)
                        " · 平均每周 ${String.format("%.1f", sessionContext.weeklyFrequency)} 次"
                    else "",
                color = PcTextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "本周还没开始训练，准备好开始了吗？💪",
                color = PcTextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Recent workout
        sessionContext.recentWorkoutSummary?.let { recent ->
            Spacer(modifier = Modifier.height(4.dp))
            val bodyPartLabel = recent.bodyPart?.chineseName ?: "训练"
            val dayLabel = when {
                recent.daysAgo == 0 -> "今天"
                recent.daysAgo == 1 -> "昨天"
                else -> "${recent.daysAgo}天前"
            }
            Text(
                text = "上次训练：${dayLabel} · $bodyPartLabel" +
                    if (recent.exerciseNames.isNotEmpty())
                        "（${recent.exerciseNames.take(3).joinToString("、")}${if (recent.exerciseNames.size > 3) "等" else ""}）"
                    else "",
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Body part coverage
        if (sessionContext.uncoveredBodyParts.isNotEmpty() && sessionContext.trainingDaysThisWeek > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            val uncoveredNames = sessionContext.uncoveredBodyParts.map { it.chineseName }
            Text(
                text = "💡 ${uncoveredNames.joinToString("、")}本周还没练，今天可以考虑。",
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Stagnation alerts
        if (sessionContext.stagnatingExercises.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            val alert = sessionContext.stagnatingExercises.first()
            Text(
                text = "⚠️「${alert.exerciseName}」已停滞 ${alert.weeksStagnant} 周，需要看看怎么突破吗？",
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Total workouts
        if (sessionContext.totalWorkoutCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "累计 ${sessionContext.totalWorkoutCount} 次训练记录 📋",
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
