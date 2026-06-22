package com.privatecoach.app.core.model

import java.io.File
import java.time.LocalDate

// ═══════════════════════════════════════════════
// Message Types (conversation bubble types)
// ═══════════════════════════════════════════════

sealed class Message {
    abstract val id: String
    abstract val timestamp: Long

    /** 用户文本消息 */
    data class UserText(
        val content: String,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** 用户语音消息 */
    data class UserVoice(
        val audioFile: File,
        val transcript: String,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** AI 纯文本/Markdown 回复 */
    data class AiText(
        val markdown: String,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** 训练记录确认卡 */
    data class ConfirmCard(
        val parsedResult: AiParsedResult,
        val sourceText: String,
        val existingWorkoutId: Long?,
        val isAppendMode: Boolean = false,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** 数据统计卡片 */
    data class DataCard(
        val title: String,
        val stats: List<StatItem>,
        val chartType: ChartType? = null,
        val chartData: ChartData? = null,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** 图表卡片 */
    data class ChartCard(
        val title: String,
        val chartType: ChartType,
        val interpretation: String,
        val chartData: ChartData? = null,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** 训练建议卡片 */
    data class AdviceCard(
        val result: AdviceResult,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** 复盘总结卡片 */
    data class SummaryCard(
        val result: ReviewResult,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()

    /** 系统提示消息 */
    data class SystemMsg(
        val text: String,
        val level: SystemMsgLevel = SystemMsgLevel.INFO,
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : Message()
}

// ═══════════════════════════════════════════════
// Supporting Types
// ═══════════════════════════════════════════════

data class StatItem(
    val label: String,
    val value: String,
    val trend: TrendDirection? = null,
    val isHighlighted: Boolean = false
)

enum class TrendDirection { UP, DOWN, STABLE }

enum class ChartType { LINE, BAR, PIE }

enum class SystemMsgLevel { INFO, WARNING, ERROR }

enum class ConversationState {
    IDLE, LOADING, RECORDING, AWAITING_CONFIRMATION
}

enum class AiAvailability { NO_KEY, OFFLINE, READY, API_ERROR }

sealed interface ConversationUiEvent {
    data object OpenDashboard : ConversationUiEvent
    data object OpenCalendar : ConversationUiEvent
    data object OpenSettings : ConversationUiEvent
    data object OpenManualEntry : ConversationUiEvent
}

// ═══════════════════════════════════════════════
// Intent & Safety
// ═══════════════════════════════════════════════

enum class IntentType {
    RECORD, QUERY, ADVICE, REVIEW, MANAGE, AMBIGUOUS
}

data class ClassifiedIntent(
    val type: IntentType,
    val confidence: Float = 1.0f,
    val entities: Map<String, String> = emptyMap()
)

enum class SafetyVerdict {
    OK, FORBIDDEN, HIGH_RISK, IRRELEVANT
}

// ═══════════════════════════════════════════════
// Quick Action Chips
// ═══════════════════════════════════════════════

data class QuickActionChip(
    val label: String,
    val emoji: String,
    val prompt: String,
    val priority: Int = 0,
    val action: QuickActionAction = QuickActionAction.PROMPT
)

enum class QuickActionAction { PROMPT, OPEN_DASHBOARD, OPEN_CALENDAR, OPEN_SETTINGS, OPEN_MANUAL_ENTRY }

sealed interface ChartData {
    data class Line(val labels: List<String>, val values: List<Double>, val seriesLabel: String) : ChartData
    data class Bars(val labels: List<String>, val values: List<Float>) : ChartData
    data class Pie(val labels: List<String>, val values: List<Float>) : ChartData
}

// ═══════════════════════════════════════════════
// Session Context (cold-start, injected into AI system prompt)
// ═══════════════════════════════════════════════

data class SessionContext(
    val trainingDaysThisWeek: Int,
    val recentWorkoutSummary: WorkoutSummary?,
    val coveredBodyParts: Set<BodyPart>,
    val uncoveredBodyParts: List<BodyPart>,
    val stagnatingExercises: List<StagnationAlert>,
    val frequentExercises: List<String>,
    val weeklyFrequency: Double,
    val totalWorkoutCount: Int
)

data class WorkoutSummary(
    val date: LocalDate,
    val bodyPart: BodyPart?,
    val type: WorkoutType,
    val exerciseNames: List<String>,
    val daysAgo: Int
)

data class StagnationAlert(
    val exerciseName: String,
    val currentWeight: Double,
    val weeksStagnant: Int,
    val lastProgressDate: LocalDate?
)

// ═══════════════════════════════════════════════
// Advice Context & Result
// ═══════════════════════════════════════════════

data class AdviceContext(
    val exerciseName: String,
    val concern: String,
    val trendSummary: String,
    val frequencySummary: String,
    val relatedExercises: List<String>,
    val bodyPartSummary: String,
    val sessionSummary: String
)

enum class DataSufficiency { SUFFICIENT, LIMITED, INSUFFICIENT }

data class AdviceResult(
    val dataSummary: String,
    val analysis: String,
    val suggestions: List<String>,
    val dataSufficiency: DataSufficiency = DataSufficiency.SUFFICIENT
)

// ═══════════════════════════════════════════════
// Review Context & Result
// ═══════════════════════════════════════════════

data class ReviewContext(
    val periodName: String,
    val trainingDays: Int,
    val totalSets: Int,
    val totalVolume: String,
    val volumeChange: String,
    val bodyPartsSummary: String,
    val previousPeriodName: String,
    val progressExercises: List<String>,
    val stagnatingSummary: String
)

data class ReviewResult(
    val overview: String,
    val highlights: List<String>,
    val concerns: List<String>,
    val comparisonText: String
)

// ═══════════════════════════════════════════════
// Query Context (for complex queries needing AI interpretation)
// ═══════════════════════════════════════════════

data class QueryContext(
    val question: String,
    val timeRangeLabel: String,
    val aggregatedData: String
)

// ═══════════════════════════════════════════════
// Query Engine Result
// ═══════════════════════════════════════════════

sealed class QueryEngineResult {
    data class LocalText(val text: String) : QueryEngineResult()
    data class ChartNeeded(
        val title: String,
        val chartType: ChartType,
        val interpretation: String,
        val chartData: ChartData
    ) : QueryEngineResult()
    data class DataCardNeeded(
        val title: String,
        val stats: List<StatItem>,
        val chartType: ChartType? = null,
        val chartData: ChartData? = null
    ) : QueryEngineResult()
    data class AiNeeded(val context: QueryContext) : QueryEngineResult()
}

// ═══════════════════════════════════════════════
// Pending Confirm (for ConfirmCard state management)
// ═══════════════════════════════════════════════

data class PendingConfirmData(
    val parsedResult: AiParsedResult,
    val sourceText: String,
    val existingWorkoutId: Long?,
    val isAppendMode: Boolean = false
)
