package com.privatecoach.app.ui.screen.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.core.analytics.ReportGenerator
import com.privatecoach.app.core.analytics.TrendAnalyzer
import com.privatecoach.app.core.analytics.VolumeCalculator
import com.privatecoach.app.core.model.MultiExerciseTrend
import com.privatecoach.app.core.model.TimeRange
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val trendAnalyzer: TrendAnalyzer,
    private val volumeCalculator: VolumeCalculator,
    private val reportGenerator: ReportGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            workoutRepository.getAllExerciseNames().collect { names ->
                _uiState.update { it.copy(allExerciseNames = names) }
            }
        }
        loadData()
    }

    fun setTab(tab: AnalysisTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
        loadData()
    }

    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(timeRange = range) }
        loadData()
    }

    fun toggleExercise(name: String) {
        _uiState.update { state ->
            val current = state.selectedExerciseNames
            val updated = if (name in current) {
                current - name
            } else {
                if (current.size >= 5) current
                else current + name
            }
            state.copy(selectedExerciseNames = updated)
        }
        viewModelScope.launch { loadTrendData() }
    }

    fun setTrendMetric(metric: TrendMetric) {
        _uiState.update { it.copy(trendMetric = metric) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, reportError = null) }
    }

    fun generateReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingReport = true, reportError = null) }

            try {
                val now = LocalDate.now()
                val (periodStart, periodEnd, periodName, prevStart, prevEnd, prevName) =
                    computeReportPeriods(now)

                val report = reportGenerator.generate(
                    periodName = periodName,
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                    previousPeriodStart = prevStart,
                    previousPeriodEnd = prevEnd,
                    previousPeriodName = prevName
                )
                _uiState.update { it.copy(currentReport = report, isGeneratingReport = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        reportError = "报告生成失败: ${e.message ?: "未知错误"}",
                        isGeneratingReport = false
                    )
                }
            }
        }
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                when (_uiState.value.selectedTab) {
                    AnalysisTab.TREND -> loadTrendData()
                    AnalysisTab.VOLUME -> loadVolumeData()
                    AnalysisTab.REPORT -> { /* Report is generated on demand */ }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "加载失败") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadTrendData() {
        val state = _uiState.value
        if (state.selectedExerciseNames.isEmpty()) {
            _uiState.update {
                it.copy(trendLines = emptyList(), isLoading = false)
            }
            return
        }

        val (start, end) = state.timeRange.toDateRange()
        val allTrends = mutableListOf<MultiExerciseTrend>()

        for (exerciseName in state.selectedExerciseNames) {
            val raw = workoutRepository.getExerciseTrendData(exerciseName)
                .filter { it.date in start..end }
            if (raw.isNotEmpty()) {
                val trend = trendAnalyzer.computeTrendForExercise(raw)
                if (trend.isNotEmpty()) {
                    allTrends.add(MultiExerciseTrend(exerciseName, trend))
                }
            }
        }

        _uiState.update { it.copy(trendLines = allTrends, isLoading = false) }
    }

    private suspend fun loadVolumeData() {
        val state = _uiState.value
        val (start, end) = state.timeRange.toDateRange()

        // Weekly volumes
        val volumeData = workoutRepository.getVolumeData(start, end)
        val weeklyVolumes = volumeCalculator.computeWeeklyVolumes(volumeData, start, end)

        // Body part distribution
        val bodyPartRaw = workoutRepository.getBodyPartDistribution(start, end)
        val bodyPartStats = volumeCalculator.computeBodyPartStats(bodyPartRaw)

        // Training frequency
        val frequency = workoutRepository.getTrainingFrequency(start, end)

        // Monthly summary overview
        val workoutsInRange = workoutRepository.getWorkoutsByDateRange(start, end)
            .firstOrNull()?.toMutableList() ?: mutableListOf()

        val summary = volumeCalculator.computeMonthlySummary(workoutsInRange)

        _uiState.update {
            it.copy(
                weeklyVolumes = weeklyVolumes,
                bodyPartStats = bodyPartStats,
                trainingFrequency = frequency,
                monthlyTotalSets = summary.totalSets,
                monthlyTotalVolume = summary.totalVolume,
                monthlyTrainingDays = summary.trainingDays,
                isLoading = false
            )
        }
    }

    private fun computeReportPeriods(now: LocalDate): ReportPeriods {
        // This week vs last week
        val thisWeekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val thisWeekEnd = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val lastWeekStart = thisWeekStart.minusWeeks(1)
        val lastWeekEnd = thisWeekStart.minusDays(1)

        return ReportPeriods(
            periodStart = thisWeekStart,
            periodEnd = thisWeekEnd,
            periodName = "本周",
            prevStart = lastWeekStart,
            prevEnd = lastWeekEnd,
            prevName = "上周"
        )
    }

    private fun TimeRange.toDateRange(): Pair<LocalDate, LocalDate> {
        val end = LocalDate.now()
        val start = when (this) {
            TimeRange.ONE_MONTH -> end.minusMonths(1)
            TimeRange.THREE_MONTHS -> end.minusMonths(3)
            TimeRange.SIX_MONTHS -> end.minusMonths(6)
            TimeRange.ALL -> LocalDate.of(2000, 1, 1)
        }
        return start to end
    }

    private data class ReportPeriods(
        val periodStart: LocalDate,
        val periodEnd: LocalDate,
        val periodName: String,
        val prevStart: LocalDate,
        val prevEnd: LocalDate,
        val prevName: String
    )
}
