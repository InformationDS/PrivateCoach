package com.privatecoach.app.ui.screen.analysis

import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.BodyPartStat
import com.privatecoach.app.core.model.MultiExerciseTrend
import com.privatecoach.app.core.model.Report
import com.privatecoach.app.core.model.TimeRange
import com.privatecoach.app.core.model.TrainingFrequencyPoint
import com.privatecoach.app.core.model.WeeklyVolume
import com.privatecoach.app.ui.component.ChartBar
import com.privatecoach.app.ui.component.ChartLine

data class AnalysisUiState(
    val selectedTab: AnalysisTab = AnalysisTab.TREND,
    val timeRange: TimeRange = TimeRange.ONE_MONTH,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Trend tab — body part based
    val selectedBodyParts: Set<BodyPart> = emptySet(),           // max 3
    val bodyPartTrendLines: List<ChartLine> = emptyList(),       // aggregated volume per body part
    val trendXLabels: List<String> = emptyList(),
    // Exercise breakdown (only when single body part selected)
    val exerciseBreakdown: List<ChartBar> = emptyList(),

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
