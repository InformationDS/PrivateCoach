package com.privatecoach.app.core.query

import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.BodyPartCount
import com.privatecoach.app.core.model.ChartType
import com.privatecoach.app.core.model.IntentType
import com.privatecoach.app.core.model.QueryContext
import com.privatecoach.app.core.model.QueryEngineResult
import com.privatecoach.app.core.model.StatItem
import com.privatecoach.app.core.model.TrendDirection
import com.privatecoach.app.domain.repository.WorkoutRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local query engine. Handles simple queries with local SQL + template responses.
 * Complex queries aggregate data and return [QueryContext] for AI interpretation.
 */
@Singleton
class QueryEngine @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend fun execute(
        intentType: IntentType,
        entities: Map<String, String>
    ): QueryEngineResult {
        val timeRange = parseTimeRange(entities["timeRange"])
        val metric = entities["metric"] ?: "training_days"
        val exercise = entities["exercise"]

        return when (metric) {
            "training_days" -> queryTrainingDays(timeRange)
            "trend" -> {
                if (exercise != null) queryExerciseTrend(exercise, timeRange)
                else QueryEngineResult.LocalText("你想查看哪个动作的趋势？比如「卧推」、「深蹲」")
            }
            "volume" -> queryVolume(timeRange)
            "distribution" -> queryBodyPartDistribution(timeRange)
            "comparison" -> queryComparison(timeRange)
            "frequency" -> queryFrequency(timeRange)
            else -> queryTrainingDays(timeRange)
        }
    }

    // ═══════════════════════════════════════════
    // Individual Query Handlers
    // ═══════════════════════════════════════════

    private suspend fun queryTrainingDays(range: TimeRange): QueryEngineResult {
        val days = workoutRepository.getTrainingDaysCount(range.start, range.end)

        val periodLabel = when (range.type) {
            "this_week" -> "本周"
            "last_week" -> "上周"
            "this_month" -> "本月"
            "last_month" -> "上月"
            else -> "该时间段"
        }

        if (days == 0) {
            return QueryEngineResult.LocalText("${periodLabel}还没有训练记录。加油开始吧！💪")
        }

        // Get body part distribution for richer response
        val bodyParts = workoutRepository.getBodyPartDistribution(range.start, range.end)
        val topPart = bodyParts.maxByOrNull { it.count }

        val extra = if (topPart != null) {
            "，练得最多的是${BodyPart.fromChinese(topPart.bodyPart)?.chineseName ?: topPart.bodyPart}"
        } else ""

        return QueryEngineResult.LocalText("${periodLabel}训练了 ${days} 天${extra}。")
    }

    private suspend fun queryExerciseTrend(
        exerciseName: String,
        range: TimeRange
    ): QueryEngineResult {
        val trendData = workoutRepository.getExerciseTrendData(exerciseName)

        if (trendData.isEmpty()) {
            return QueryEngineResult.LocalText("还没有「${exerciseName}」的训练记录。")
        }

        if (trendData.size < 2) {
            val point = trendData.first()
            return QueryEngineResult.LocalText(
                "「${exerciseName}」目前只有 1 条记录：${point.weight}kg × ${point.sets}组 × ${point.reps}次。多练几次就能看到趋势了。"
            )
        }

        // Compute simple stats for interpretation
        val weights = trendData.mapNotNull { it.weight }
        val firstWeight = weights.firstOrNull() ?: 0.0
        val lastWeight = weights.lastOrNull() ?: 0.0
        val trend = when {
            lastWeight > firstWeight * 1.05 -> TrendDirection.UP
            lastWeight < firstWeight * 0.95 -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }
        val latest = trendData.last()

        val trendLabel = when (trend) {
            TrendDirection.UP -> "📈 上升"
            TrendDirection.DOWN -> "📉 下降"
            TrendDirection.STABLE -> "➡️ 稳定"
        }

        val stats = listOf(
            StatItem("最大重量", "${latest.weight?.toInt() ?: "--"} kg", isHighlighted = true),
            StatItem("趋势", trendLabel),
            StatItem("数据点数", "${trendData.size} 条"),
            StatItem("最新记录", "${latest.sets}组×${latest.reps}次")
        )

        val interpretation = buildString {
            append("「${exerciseName}」共 ${trendData.size} 条记录。")
            append("最新：${latest.weight?.toInt() ?: "--"}kg × ${latest.sets}组 × ${latest.reps}次。")
            when (trend) {
                TrendDirection.UP -> append("重量整体呈上升趋势，继续加油！")
                TrendDirection.DOWN -> append("重量有所下降，可能需要关注恢复或训练质量。")
                TrendDirection.STABLE -> append("重量基本保持稳定。")
            }
        }

        return QueryEngineResult.DataCardNeeded(
            title = "${exerciseName}趋势",
            stats = stats,
            chartType = ChartType.LINE
        )
        // Note: the actual chart data will be rendered by ChartCard using trendData
    }

    private suspend fun queryVolume(range: TimeRange): QueryEngineResult {
        val volumeData = workoutRepository.getVolumeData(range.start, range.end)

        if (volumeData.isEmpty()) {
            return QueryEngineResult.LocalText("该时间段没有训练容量数据。")
        }

        val totalVolume = volumeData.sumOf {
            (it.weight ?: 0.0) * (it.sets ?: 0) * (it.reps ?: 0)
        }

        val totalSets = volumeData.sumOf { it.sets ?: 0 }
        val totalReps = volumeData.sumOf { it.reps ?: 0 }

        val stats = listOf(
            StatItem("总容量", "${totalVolume.toInt()} kg", isHighlighted = true),
            StatItem("总组数", "$totalSets 组"),
            StatItem("总次数", "$totalReps 次"),
            StatItem("训练天数", "${volumeData.map { it.workoutDate }.distinct().size} 天")
        )

        return QueryEngineResult.DataCardNeeded(
            title = "训练容量统计",
            stats = stats,
            chartType = ChartType.BAR
        )
    }

    private suspend fun queryBodyPartDistribution(range: TimeRange): QueryEngineResult {
        val distribution = workoutRepository.getBodyPartDistribution(range.start, range.end)

        if (distribution.isEmpty()) {
            return QueryEngineResult.LocalText("该时间段没有训练部位分布数据。")
        }

        val total = distribution.sumOf { it.count }
        val stats = distribution.map { entry ->
            val pct = if (total > 0) (entry.count * 100 / total) else 0
            val barGraph = "█".repeat(pct / 5) + "░".repeat(20 - pct / 5)
            StatItem(
                label = BodyPart.fromChinese(entry.bodyPart)?.chineseName ?: entry.bodyPart,
                value = "$pct% ($barGraph)",
                isHighlighted = entry.count == distribution.maxOf { it.count }
            )
        }

        val top = distribution.maxByOrNull { it.count }
        val topName = top?.let {
            BodyPart.fromChinese(it.bodyPart)?.chineseName ?: it.bodyPart
        }

        return QueryEngineResult.DataCardNeeded(
            title = "训练部位分布",
            stats = stats,
            chartType = ChartType.PIE
        )
    }

    private suspend fun queryComparison(range: TimeRange): QueryEngineResult {
        // Auto-derive previous period of same length
        val periodDays = range.start.until(range.end).days.toLong()
        val prevRange = TimeRange(
            start = range.start.minusDays(periodDays + 1),
            end = range.start.minusDays(1),
            type = "previous_${range.type}",
            label = "上周期"
        )

        val currentDays = workoutRepository.getTrainingDaysCount(range.start, range.end)
        val previousDays = workoutRepository.getTrainingDaysCount(prevRange.start, prevRange.end)

        val diff = currentDays - previousDays
        val diffText = when {
            diff > 0 -> "多 ${diff} 天 ↑"
            diff < 0 -> "少 ${-diff} 天 ↓"
            else -> "持平 →"
        }

        val stats = listOf(
            StatItem("本周期", "${currentDays} 天"),
            StatItem("上周期", "${previousDays} 天"),
            StatItem("变化", diffText, isHighlighted = true)
        )

        return QueryEngineResult.DataCardNeeded(
            title = "训练天数对比",
            stats = stats
        )
    }

    private suspend fun queryFrequency(range: TimeRange): QueryEngineResult {
        val frequency = workoutRepository.getTrainingFrequency(range.start, range.end)

        if (frequency.isEmpty()) {
            return QueryEngineResult.LocalText("该时间段没有训练频率数据。")
        }

        val totalDays = frequency.size
        val totalSessions = frequency.sumOf { it.sessions }

        val avgPerWeek = if (totalDays > 0) {
            (totalSessions.toDouble() / totalDays * 7)
        } else 0.0

        val evaluation = when {
            avgPerWeek >= 5 -> "频率很高，注意恢复 🔥"
            avgPerWeek >= 3 -> "频率适中，保持节奏 👍"
            avgPerWeek >= 2 -> "频率偏低，可以考虑增加 📈"
            else -> "频率较低，建议增加到每周2-3次 💪"
        }

        val stats = listOf(
            StatItem("平均每周", String.format("%.1f 次", avgPerWeek), isHighlighted = true),
            StatItem("总训练天数", "$totalDays 天"),
            StatItem("总训练次数", "$totalSessions 次"),
            StatItem("评估", evaluation)
        )

        return QueryEngineResult.DataCardNeeded(
            title = "训练频率分析",
            stats = stats,
            chartType = ChartType.BAR
        )
    }

    // ═══════════════════════════════════════════
    // Time Range Parsing
    // ═══════════════════════════════════════════

    data class TimeRange(
        val start: LocalDate,
        val end: LocalDate,
        val type: String,
        val label: String
    )

    private fun parseTimeRange(key: String?): TimeRange {
        val today = LocalDate.now()
        return when (key) {
            "today" -> TimeRange(today, today, "today", "今天")
            "yesterday" -> TimeRange(
                today.minusDays(1), today.minusDays(1),
                "yesterday", "昨天"
            )
            "this_week" -> {
                val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                TimeRange(start, today, "this_week", "本周")
            }
            "last_week" -> {
                val start = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                    .with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                val end = start.plusDays(6)
                TimeRange(start, end, "last_week", "上周")
            }
            "this_month" -> {
                val start = today.withDayOfMonth(1)
                TimeRange(start, today, "this_month", "本月")
            }
            "last_month" -> {
                val start = today.minusMonths(1).withDayOfMonth(1)
                val end = start.plusMonths(1).minusDays(1)
                TimeRange(start, end, "last_month", "上月")
            }
            "all" -> TimeRange(
                LocalDate.of(2020, 1, 1), today,
                "all", "全部"
            )
            else -> {
                // Default to this week
                val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                TimeRange(start, today, "this_week", "本周")
            }
        }
    }
}
