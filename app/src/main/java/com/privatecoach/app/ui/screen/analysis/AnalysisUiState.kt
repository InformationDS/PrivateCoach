package com.privatecoach.app.ui.screen.analysis

import com.privatecoach.app.core.model.BodyPartStat
import com.privatecoach.app.core.model.MultiExerciseTrend
import com.privatecoach.app.core.model.Report
import com.privatecoach.app.core.model.TimeRange
import com.privatecoach.app.core.model.TrainingFrequencyPoint
import com.privatecoach.app.core.model.WeeklyVolume

data class AnalysisUiState(
    val selectedTab: AnalysisTab = AnalysisTab.TREND,
    val timeRange: TimeRange = TimeRange.ONE_MONTH,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Trend tab
    val allExerciseNames: List<String> = emptyList(),
    val selectedExerciseNames: Set<String> = emptySet(),
    val trendLines: List<MultiExerciseTrend> = emptyList(),
    val trendMetric: TrendMetric = TrendMetric.MAX_WEIGHT,

    // Volume tab
    val weeklyVolumes: List<WeeklyVolume> = emptyList(),
    val bodyPartStats: List<BodyPartStat> = emptyList(),
    val trainingFrequency: List<TrainingFrequencyPoint> = emptyList(),
    val monthlyTotalSets: Int = 0,
    val monthlyTotalVolume: Double = 0.0,
    val monthlyTrainingDays: Int = 0,

    // Report tab
    val isGeneratingReport: Boolean = false,
    val currentReport: Report? = null,
    val reportError: String? = null
)

enum class AnalysisTab(val label: String) {
    TREND("趋势"),
    VOLUME("体量"),
    REPORT("报告")
}

enum class TrendMetric(val label: String) {
    MAX_WEIGHT("最大重量"),
    TOTAL_VOLUME("总容量"),
    AVG_REPS("平均次数")
}
