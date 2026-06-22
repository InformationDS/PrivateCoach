package com.privatecoach.app.ui.screen.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import com.privatecoach.app.core.audio.AudioRecorder
import com.privatecoach.app.core.ai.AiApiService
import com.privatecoach.app.core.ai.IntentRouter
import com.privatecoach.app.core.ai.SessionContextBuilder
import com.privatecoach.app.core.model.AdviceContext
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.ClassifiedIntent
import com.privatecoach.app.core.model.ConversationState
import com.privatecoach.app.core.model.IntentType
import com.privatecoach.app.core.model.Message
import com.privatecoach.app.core.model.PendingConfirmData
import com.privatecoach.app.core.model.QuickActionChip
import com.privatecoach.app.core.model.QueryContext
import com.privatecoach.app.core.model.QueryEngineResult
import com.privatecoach.app.core.model.ReviewContext
import com.privatecoach.app.core.model.SafetyVerdict
import com.privatecoach.app.core.model.SameDayWriteMode
import com.privatecoach.app.core.model.AiAvailability
import com.privatecoach.app.core.model.ConversationUiEvent
import com.privatecoach.app.core.model.QuickActionAction
import com.privatecoach.app.core.model.SessionContext
import com.privatecoach.app.core.model.CardioDetail
import com.privatecoach.app.core.model.Exercise
import com.privatecoach.app.core.model.InputMode
import com.privatecoach.app.core.model.ParsedExercise
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.core.query.QueryEngine
import com.privatecoach.app.domain.repository.SettingsRepository
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiApiService: AiApiService,
    private val intentRouter: IntentRouter,
    private val queryEngine: QueryEngine,
    private val sessionContextBuilder: SessionContextBuilder,
    private val settingsRepository: SettingsRepository,
    private val audioRecorder: AudioRecorder,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    // ── State ──

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _state = MutableStateFlow(ConversationState.IDLE)
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private val _pendingConfirm = MutableStateFlow<PendingConfirmData?>(null)
    val pendingConfirm: StateFlow<PendingConfirmData?> = _pendingConfirm.asStateFlow()

    private val _sessionContext = MutableStateFlow<SessionContext?>(null)
    val sessionContext: StateFlow<SessionContext?> = _sessionContext.asStateFlow()

    private val _quickActions = MutableStateFlow<List<QuickActionChip>>(emptyList())
    val quickActions: StateFlow<List<QuickActionChip>> = _quickActions.asStateFlow()

    private val _aiAvailability = MutableStateFlow(AiAvailability.NO_KEY)
    val aiAvailability: StateFlow<AiAvailability> = _aiAvailability.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ConversationUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<ConversationUiEvent> = _uiEvents.asSharedFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private var recordingFile: File? = null

    // ── Init ──

    init {
        viewModelScope.launch {
            val ctx = sessionContextBuilder.build()
            _sessionContext.value = ctx
            showWelcomeMessage(ctx)
            generateQuickActions(ctx)
        }
        viewModelScope.launch {
            combine(
                settingsRepository.apiKey,
                settingsRepository.apiEndpoint,
                settingsRepository.modelName
            ) { key, endpoint, model ->
                Triple(key, endpoint, model)
            }.collectLatest { (key, endpoint, model) ->
                _aiAvailability.value = when {
                    key.isBlank() || endpoint.isBlank() || model.isBlank() -> AiAvailability.NO_KEY
                    !hasNetwork() -> AiAvailability.OFFLINE
                    else -> AiAvailability.READY
                }
                _sessionContext.value?.let(::generateQuickActions)
            }
        }
    }

    // ═══════════════════════════════════════════
    // Input Handling
    // ═══════════════════════════════════════════

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun onSend() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        _inputText.value = ""
        handleTextInput(text)
    }

    fun onQuickActionClick(chip: QuickActionChip) {
        when (chip.action) {
            QuickActionAction.PROMPT -> handleTextInput(chip.prompt)
            QuickActionAction.OPEN_DASHBOARD -> _uiEvents.tryEmit(ConversationUiEvent.OpenDashboard)
            QuickActionAction.OPEN_CALENDAR -> _uiEvents.tryEmit(ConversationUiEvent.OpenCalendar)
            QuickActionAction.OPEN_SETTINGS -> _uiEvents.tryEmit(ConversationUiEvent.OpenSettings)
            QuickActionAction.OPEN_MANUAL_ENTRY -> _uiEvents.tryEmit(ConversationUiEvent.OpenManualEntry)
        }
    }

    fun handleTextInput(text: String) {
        viewModelScope.launch {
            refreshNetworkAvailability()
            if (_pendingConfirm.value != null && text.contains(Regex("再加|追加|补充|还做了|加一个"))) {
                addMessage(Message.UserText(text))
                appendToPendingWorkout(text)
                return@launch
            }
            // 1. Safety check
            val safety = intentRouter.checkSafety(text)
            when (safety) {
                SafetyVerdict.FORBIDDEN -> {
                    addMessage(Message.SystemMsg(
                        "出于安全考虑，此操作不能通过 AI 执行。请在相应页面手动操作。",
                        com.privatecoach.app.core.model.SystemMsgLevel.WARNING
                    ))
                    return@launch
                }
                SafetyVerdict.HIGH_RISK -> {
                    addMessage(Message.AiText(
                        "这涉及健康风险，我不能替代医生或康复师判断。" +
                            "建议先暂停相关动作并寻求专业医疗意见。如果需要，我可以帮你在备注中记录这个情况。"
                    ))
                    return@launch
                }
                SafetyVerdict.IRRELEVANT -> {
                    addMessage(Message.AiText(
                        "我主要帮你记录和分析训练。你可以试试说：\n" +
                            "· 「记录今天的训练」\n· 「这周练了几次？」\n· 「卧推最近有进步吗？」\n· 「给我一些训练建议」"
                    ))
                    return@launch
                }
                SafetyVerdict.OK -> {}
            }

            // 2. Add user message
            addMessage(Message.UserText(text))

            // 3. Intent classification
            val intent = intentRouter.classify(text)

            // 4. Dispatch to handler
            when (intent.type) {
                IntentType.RECORD -> handleRecord(text, intent)
                IntentType.QUERY -> handleQuery(text, intent)
                IntentType.ADVICE -> handleAdvice(text, intent)
                IntentType.REVIEW -> handleReview(text, intent)
                IntentType.MANAGE -> handleManage(text, intent)
                IntentType.AMBIGUOUS -> handleAmbiguous(text)
            }
        }
    }

    fun handleVoiceInput(audioFile: File) {
        viewModelScope.launch {
            if (!isRemoteAiReady()) {
                addMessage(Message.SystemMsg("AI 暂不可用，请使用文字输入或手动录入。"))
                return@launch
            }

            _state.value = ConversationState.LOADING
            recordingFile = audioFile

            val result = aiApiService.transcribeAndParse(audioFile)
            result.fold(
                onSuccess = { parsed ->
                    addMessage(Message.UserVoice(audioFile, parsed.summaryMarkdown.take(200)))
                    showConfirmCard(parsed, parsed.summaryMarkdown)
                },
                onFailure = { e ->
                    markApiError()
                    addMessage(Message.SystemMsg(
                        "语音识别失败：${e.message ?: "未知错误"}，请重试或使用文字输入。"
                    ))
                }
            )
            _state.value = ConversationState.IDLE
        }
    }

    // ═══════════════════════════════════════════
    // Intent Handlers
    // ═══════════════════════════════════════════

    private suspend fun handleRecord(text: String, intent: ClassifiedIntent) {
        if (!isRemoteAiReady()) {
            addMessage(Message.SystemMsg("AI 暂不可用，请在训练记录页手动录入。"))
            return
        }

        _state.value = ConversationState.LOADING

        val result = aiApiService.parseText(text)
        result.fold(
            onSuccess = { parsed ->
                showConfirmCard(parsed, text)
            },
            onFailure = { e ->
                markApiError()
                addMessage(Message.SystemMsg(
                    "AI 解析失败：${e.message ?: "未知错误"}，请重试。"
                ))
            }
        )
        _state.value = ConversationState.IDLE
    }

    private suspend fun handleQuery(text: String, intent: ClassifiedIntent) {
        val entities = intentRouter.extractEntities(text, IntentType.QUERY)
        val queryResult = queryEngine.execute(IntentType.QUERY, entities)

        when (queryResult) {
            is QueryEngineResult.LocalText -> {
                addMessage(Message.AiText(queryResult.text))
            }
            is QueryEngineResult.DataCardNeeded -> {
                addMessage(Message.DataCard(
                    title = queryResult.title,
                    stats = queryResult.stats,
                    chartType = queryResult.chartType,
                    chartData = queryResult.chartData
                ))
            }
            is QueryEngineResult.ChartNeeded -> {
                addMessage(Message.ChartCard(
                    title = queryResult.title,
                    chartType = queryResult.chartType,
                    interpretation = queryResult.interpretation,
                    chartData = queryResult.chartData
                ))
            }
            is QueryEngineResult.AiNeeded -> {
                // Complex query — delegate to AI
                if (isRemoteAiReady()) {
                    val aiResult = aiApiService.interpretQuery(queryResult.context)
                    aiResult.fold(
                        onSuccess = { interpretation ->
                            if (interpretation.isNotBlank()) {
                                addMessage(Message.AiText(interpretation))
                            }
                        },
                        onFailure = {
                            markApiError()
                            addMessage(Message.SystemMsg("AI 解释暂不可用，本地统计仍可继续使用。"))
                        }
                    )
                }
            }
        }
    }

    private suspend fun handleAdvice(text: String, intent: ClassifiedIntent) {
        if (!isRemoteAiReady()) {
            addMessage(Message.SystemMsg("AI 暂不可用，无法生成个性化建议。"))
            return
        }

        _state.value = ConversationState.LOADING
        val entities = intentRouter.extractEntities(text, IntentType.ADVICE)
        val exerciseName = entities["exercise"] ?: ""
        val concern = entities["concern"] ?: "general"
        val ctx = _sessionContext.value

        // Gather data for advice
        val trendData = if (exerciseName.isNotBlank()) {
            workoutRepository.getExerciseTrendData(exerciseName)
        } else emptyList()

        val now = LocalDate.now()
        val frequencyData = workoutRepository.getTrainingFrequency(now.minusWeeks(12), now)
        val bodyParts = workoutRepository.getBodyPartDistribution(now.minusWeeks(12), now)

        val adviceContext = AdviceContext(
            exerciseName = exerciseName.ifBlank { "综合训练" },
            concern = concern,
            trendSummary = buildTrendSummary(trendData, exerciseName),
            frequencySummary = buildFrequencySummary(frequencyData),
            relatedExercises = extractRelatedExercises(exerciseName, ctx),
            bodyPartSummary = bodyParts.joinToString("; ") {
                "${it.bodyPart}(${it.count}次)"
            },
            sessionSummary = buildSessionSummary(ctx)
        )

        val result = aiApiService.generateAdvice(adviceContext)
        result.fold(
            onSuccess = { advice ->
                addMessage(Message.AdviceCard(advice))
            },
            onFailure = { e ->
                markApiError()
                addMessage(Message.SystemMsg(
                    "建议生成失败：${e.message ?: "未知错误"}"
                ))
            }
        )
        _state.value = ConversationState.IDLE
    }

    private suspend fun handleReview(text: String, intent: ClassifiedIntent) {
        if (!isRemoteAiReady()) {
            // Fallback to local stats
            val entities = intentRouter.extractEntities(text, IntentType.REVIEW)
            val queryResult = queryEngine.execute(IntentType.QUERY, entities)
            if (queryResult is QueryEngineResult.LocalText) {
                addMessage(Message.AiText(queryResult.text))
            }
            return
        }

        _state.value = ConversationState.LOADING
        val entities = intentRouter.extractEntities(text, IntentType.REVIEW)
        val period = entities["period"] ?: "this_week"

        val now = LocalDate.now()
        val reviewTimeRange = buildReviewTimeRange(period, now)
        val start = reviewTimeRange.start
        val end = reviewTimeRange.end
        val label = reviewTimeRange.label
        val prevLabel = reviewTimeRange.prevLabel

        val trainingDays = workoutRepository.getTrainingDaysCount(start, end)
        val allWorkouts = workoutRepository.getWorkoutsByDateRange(start, end)
        val workoutsList = allWorkouts.first()

        val totalSets = workoutsList.sumOf { w -> w.exercises.sumOf { it.sets ?: 0 } }
        val totalVolume = workoutsList.sumOf { w ->
            w.exercises.sumOf { (it.weight ?: 0.0) * (it.sets ?: 0) * (it.reps ?: 0) }
        }

        // Previous period
        val periodDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1
        val prevStart = start.minusDays(periodDays)
        val prevEnd = start.minusDays(1)
        val prevDays = workoutRepository.getTrainingDaysCount(prevStart, prevEnd)
        val volumeChange = if (prevDays > 0) "${((trainingDays - prevDays).toDouble() / prevDays * 100).toInt()}%" else "首周"

        val bodyParts = workoutRepository.getBodyPartDistribution(start, end)

        val reviewContext = ReviewContext(
            periodName = label,
            trainingDays = trainingDays,
            totalSets = totalSets,
            totalVolume = "${totalVolume.toInt()}kg",
            volumeChange = volumeChange,
            bodyPartsSummary = bodyParts.joinToString("; ") { "${it.bodyPart}(${it.count})" },
            previousPeriodName = prevLabel,
            progressExercises = emptyList(),
            stagnatingSummary = _sessionContext.value?.stagnatingExercises
                ?.joinToString(", ") { "${it.exerciseName}(${it.weeksStagnant}周)" } ?: "无"
        )

        val result = aiApiService.generateReview(reviewContext)
        result.fold(
            onSuccess = { review ->
                addMessage(Message.SummaryCard(review))
            },
            onFailure = { e ->
                markApiError()
                addMessage(Message.SystemMsg("复盘生成失败：${e.message ?: "未知错误"}"))
            }
        )
        _state.value = ConversationState.IDLE
    }

    private fun handleManage(text: String, intent: ClassifiedIntent) {
        val entities = intentRouter.extractEntities(text, IntentType.MANAGE)
        when (entities["action"]) {
            "delete" -> {
                addMessage(Message.SystemMsg(
                    "请到训练记录页面手动删除，以防误操作。",
                    com.privatecoach.app.core.model.SystemMsgLevel.WARNING
                ))
            }
            "create_template" -> {
                addMessage(Message.SystemMsg(
                    "模板功能请在训练记录详情页或模板管理页操作。"
                ))
            }
            else -> {
                addMessage(Message.AiText("我没有理解你想做什么，能换个方式说吗？"))
            }
        }
    }

    private fun handleAmbiguous(text: String) {
        addMessage(Message.AiText(
            "你想让我帮你做什么？\n" +
                "1. 🏋️ 记录训练\n" +
                "2. 📊 查看训练数据\n" +
                "3. 💡 获取训练建议\n" +
                "4. 📋 总结训练情况"
        ))
    }

    // ═══════════════════════════════════════════
    // Confirm Card Management
    // ═══════════════════════════════════════════

    private suspend fun showConfirmCard(parsed: AiParsedResult, sourceText: String) {
        // Check if there's an existing workout today
        val today = LocalDate.now()
        val workotusToday = workoutRepository.getWorkoutsByDateRange(today, today).first()

        if (workotusToday.isNotEmpty()) {
            _pendingConfirm.value = PendingConfirmData(
                parsedResult = parsed,
                sourceText = sourceText,
                existingWorkoutId = workotusToday.first().id,
                isAppendMode = true
            )
        } else {
            _pendingConfirm.value = PendingConfirmData(
                parsedResult = parsed,
                sourceText = sourceText,
                existingWorkoutId = null,
                isAppendMode = false
            )
        }
        _state.value = ConversationState.AWAITING_CONFIRMATION
    }

    fun confirmWorkout(mode: SameDayWriteMode = SameDayWriteMode.APPEND) {
        viewModelScope.launch {
            val pending = _pendingConfirm.value ?: return@launch
            val parsedResult = pending.parsedResult
            validateDraft(parsedResult)?.let { error ->
                addMessage(Message.SystemMsg(error, com.privatecoach.app.core.model.SystemMsgLevel.WARNING))
                return@launch
            }
            _state.value = ConversationState.LOADING
            val persistedAudio = persistRecordingIfNeeded(recordingFile)
            val workout = Workout(
                date = LocalDate.now(),
                type = parsedResult.type,
                bodyPart = parsedResult.bodyPart,
                aiSummary = parsedResult.summaryMarkdown,
                rawTranscript = pending.sourceText,
                audioFilePath = persistedAudio?.absolutePath,
                inputMode = if (recordingFile != null) InputMode.VOICE else InputMode.TEXT,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                exercises = parsedResult.exercises.mapIndexed { index, pe ->
                    Exercise(
                        name = pe.name,
                        weight = pe.weight,
                        weightUnit = pe.weightUnit,
                        sets = pe.sets,
                        reps = pe.reps,
                        duration = pe.duration,
                        distance = pe.distance,
                        sortOrder = index,
                        feeling = pe.feeling,
                        cardioDetail = parsedResult.cardioDetail?.let { cd ->
                            CardioDetail(
                                durationSeconds = cd.duration ?: pe.duration ?: 0,
                                distanceKm = cd.distance ?: pe.distance,
                                avgHeartRate = cd.avgHeartRate,
                                calories = cd.calories,
                                cardioType = cd.cardioType
                            )
                        }
                    )
                }
            )

            val saveResult = runCatching { workoutRepository.saveWorkout(workout, mode) }
            if (saveResult.isFailure) {
                persistedAudio?.delete()
                addMessage(Message.SystemMsg("保存失败：${saveResult.exceptionOrNull()?.message ?: "未知错误"}"))
                _state.value = ConversationState.AWAITING_CONFIRMATION
                return@launch
            }
            recordingFile = null

            _pendingConfirm.value = null
            addMessage(Message.AiText(
                "✅ 已保存！${parsedResult.summaryMarkdown.take(200)}"
            ))

            // Refresh session context and quick actions
            val ctx = sessionContextBuilder.build()
            _sessionContext.value = ctx
            generateQuickActions(ctx)

            _state.value = ConversationState.IDLE
        }
    }

    fun cancelWorkout() {
        _pendingConfirm.value = null
        recordingFile?.delete()
        recordingFile = null
        _state.value = ConversationState.IDLE
        addMessage(Message.SystemMsg("已取消"))
    }

    private suspend fun appendToPendingWorkout(text: String) {
        if (!isRemoteAiReady()) return
        _state.value = ConversationState.LOADING
        aiApiService.parseText(text).fold(
            onSuccess = { parsed ->
                val current = _pendingConfirm.value ?: return
                _pendingConfirm.value = current.copy(parsedResult = current.parsedResult.copy(
                    exercises = current.parsedResult.exercises + parsed.exercises
                ))
                addMessage(Message.SystemMsg("已添加 ${parsed.exercises.size} 个动作"))
            },
            onFailure = { markApiError(); addMessage(Message.SystemMsg("追加失败：${it.message ?: "未知错误"}")) }
        )
        _state.value = ConversationState.AWAITING_CONFIRMATION
    }

    fun editWorkout() {
        // Navigate to ManualEntry — handled by Compose navigation callback
    }

    fun onStartRecording() {
        viewModelScope.launch {
            if (!isRemoteAiReady()) return@launch
            runCatching {
                val dir = File(appContext.cacheDir, "recordings").apply { mkdirs() }
                val file = File.createTempFile("workout_", ".m4a", dir)
                audioRecorder.startRecording(file)
                recordingFile = file
                _state.value = ConversationState.RECORDING
            }.onFailure { addMessage(Message.SystemMsg("无法开始录音：${it.message}")) }
        }
    }

    fun onStopRecording() {
        viewModelScope.launch {
            runCatching { audioRecorder.stopRecording() }
                .onSuccess { handleVoiceInput(it) }
                .onFailure { addMessage(Message.SystemMsg("录音失败：${it.message}")) }
            _state.value = ConversationState.IDLE
        }
    }

    fun onCancelRecording() {
        _state.value = ConversationState.IDLE
        audioRecorder.release()
        recordingFile?.delete()
        recordingFile = null
        addMessage(Message.SystemMsg("录音已取消"))
    }

    // ═══════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════

    private fun addMessage(message: Message) {
        _messages.update { it + message }
    }

    private fun isRemoteAiReady() = _aiAvailability.value == AiAvailability.READY

    private suspend fun showWelcomeMessage(ctx: SessionContext) {
        val parts = mutableListOf<String>()

        if (ctx.trainingDaysThisWeek > 0) {
            parts.add("本周已训练 ${ctx.trainingDaysThisWeek} 次")
        } else {
            parts.add("本周还没开始训练")
        }

        ctx.recentWorkoutSummary?.let { recent ->
            val dayLabel = when {
                recent.daysAgo == 0 -> "今天"
                recent.daysAgo == 1 -> "昨天"
                else -> "${recent.daysAgo}天前"
            }
            val bp = recent.bodyPart?.chineseName ?: "训练"
            parts.add("上次训练：$dayLabel · $bp")
        }

        if (ctx.uncoveredBodyParts.isNotEmpty() && ctx.trainingDaysThisWeek > 0) {
            val names = ctx.uncoveredBodyParts.map { it.chineseName }.joinToString("、")
            parts.add("💡 $names 本周还没练")
        }

        if (ctx.stagnatingExercises.isNotEmpty()) {
            val alert = ctx.stagnatingExercises.first()
            parts.add("⚠️「${alert.exerciseName}」已停滞 ${alert.weeksStagnant} 周")
        }

        parts.add("有什么我可以帮你的？")

        // Don't add as persistent message - it's shown in WelcomeHeader
    }

    private fun generateQuickActions(ctx: SessionContext) {
        val chips = mutableListOf<QuickActionChip>()

        if (isRemoteAiReady()) {
            chips.add(QuickActionChip("记录训练", "🏋️", "今天练了", priority = 0))
        } else {
            chips.add(QuickActionChip("手动录入", "✍️", "", priority = 0, action = QuickActionAction.OPEN_MANUAL_ENTRY))
        }

        // Show progress if there's data
        if (ctx.trainingDaysThisWeek > 0) {
            chips.add(QuickActionChip("本周进度", "📊", "这周练得怎么样", priority = 1))
        }

        // Show advice if stagnation detected (promote to front)
        if (isRemoteAiReady() && ctx.stagnatingExercises.isNotEmpty()) {
            chips.add(QuickActionChip("突破建议", "💡", "${ctx.stagnatingExercises.first().exerciseName}卡住了怎么办", priority = 2))
        } else if (isRemoteAiReady() && ctx.totalWorkoutCount > 5) {
            chips.add(QuickActionChip("获取建议", "💡", "给我一些训练建议", priority = 3))
        }

        // Show recap
        if (isRemoteAiReady() && ctx.trainingDaysThisWeek > 0) {
            chips.add(QuickActionChip("总结这周", "📋", "帮我总结这周", priority = 4))
        }

        // Always show calendar
        chips.add(QuickActionChip("打开日历", "📅", "", priority = 5, action = QuickActionAction.OPEN_CALENDAR))

        _quickActions.value = chips.sortedBy { it.priority }
    }

    private fun buildTrendSummary(
        data: List<com.privatecoach.app.core.model.ExerciseTrendPoint>,
        exerciseName: String
    ): String {
        if (data.isEmpty()) return "无「${exerciseName}」历史数据"
        val weights = data.mapNotNull { it.weight }
        if (weights.size < 2) return "仅1条记录：${weights.firstOrNull() ?: 0.0}kg"
        val first = weights.first()
        val last = weights.last()
        val change = ((last - first) / first * 100).let { String.format("%.1f%%", it) }
        return "${data.size}条记录, ${first}kg→${last}kg, 变化$change"
    }

    private fun buildFrequencySummary(
        data: List<com.privatecoach.app.core.model.TrainingFrequencyPoint>
    ): String {
        if (data.isEmpty()) return "无频率数据"
        val total = data.sumOf { it.sessions }
        val weeks = (data.size / 7.0).coerceAtLeast(1.0)
        return "平均每周${String.format("%.1f", total / weeks)}次"
    }

    private fun extractRelatedExercises(
        exerciseName: String,
        ctx: SessionContext?
    ): List<String> {
        if (exerciseName.isBlank() || ctx == null) return emptyList()
        // Return other exercises from frequent list
        return ctx.frequentExercises.filter { it != exerciseName }.take(5)
    }

    private fun buildSessionSummary(ctx: SessionContext?): String {
        if (ctx == null) return "暂无训练数据"
        return "累计${ctx.totalWorkoutCount}次训练, 常用动作${ctx.frequentExercises.take(5).joinToString("、")}"
    }

    private data class ReviewTimeRange(
        val start: LocalDate,
        val end: LocalDate,
        val label: String,
        val prevLabel: String
    )

    private fun buildReviewTimeRange(period: String, now: LocalDate): ReviewTimeRange {
        return when (period) {
            "this_week" -> {
                val s = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                ReviewTimeRange(s, now, "本周", "上周")
            }
            "this_month" -> {
                val s = now.withDayOfMonth(1)
                ReviewTimeRange(s, now, "本月", "上月")
            }
            else -> {
                val s = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                ReviewTimeRange(s, now, "本周", "上周")
            }
        }
    }

    fun updateDraftExercise(index: Int, exercise: ParsedExercise) {
        _pendingConfirm.update { pending ->
            pending?.copy(parsedResult = pending.parsedResult.copy(
                exercises = pending.parsedResult.exercises.toMutableList().also {
                    if (index in it.indices) it[index] = exercise
                }
            ))
        }
    }

    fun addDraftExercise() {
        _pendingConfirm.update { pending ->
            pending?.copy(parsedResult = pending.parsedResult.copy(
                exercises = pending.parsedResult.exercises + ParsedExercise("", null, "kg", null, null, null, null)
            ))
        }
    }

    fun removeDraftExercise(index: Int) {
        _pendingConfirm.update { pending ->
            pending?.copy(parsedResult = pending.parsedResult.copy(
                exercises = pending.parsedResult.exercises.filterIndexed { itemIndex, _ -> itemIndex != index }
            ))
        }
    }

    fun updateDraftFeeling(feeling: com.privatecoach.app.core.model.Feeling) {
        _pendingConfirm.update { pending ->
            pending?.copy(parsedResult = pending.parsedResult.copy(
                exercises = pending.parsedResult.exercises.map { it.copy(feeling = feeling) }
            ))
        }
    }

    private fun hasNetwork(): Boolean {
        val manager = appContext.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        return manager.getNetworkCapabilities(network)
            ?.let {
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } == true
    }

    private fun refreshNetworkAvailability() {
        if (_aiAvailability.value != AiAvailability.NO_KEY) {
            _aiAvailability.value = if (hasNetwork()) AiAvailability.READY else AiAvailability.OFFLINE
        }
    }

    private fun markApiError() {
        _aiAvailability.value = AiAvailability.API_ERROR
        _sessionContext.value?.let(::generateQuickActions)
    }

    private fun validateDraft(result: AiParsedResult): String? {
        if (result.exercises.isEmpty()) return "至少需要一个有效动作"
        if (result.exercises.any { it.name.isBlank() }) return "请补全动作名称"
        if (result.exercises.any {
                (it.weight != null && it.weight <= 0) ||
                    (it.sets != null && it.sets <= 0) ||
                    (it.reps != null && it.reps <= 0) ||
                    (it.duration != null && it.duration <= 0)
            }) return "重量、组数、次数和时长必须为正数"
        val heartRate = result.cardioDetail?.avgHeartRate
        if (heartRate != null && heartRate !in 30..240) return "平均心率应在 30–240 之间"
        return null
    }

    private fun persistRecordingIfNeeded(file: File?): File? {
        if (file == null || !file.exists()) return null
        val targetDir = File(appContext.filesDir, "recordings").apply { mkdirs() }
        val target = File(targetDir, "${System.currentTimeMillis()}.m4a")
        if (file.renameTo(target)) return target
        file.copyTo(target, overwrite = true)
        file.delete()
        return target
    }

    override fun onCleared() {
        audioRecorder.release()
        if (_pendingConfirm.value == null) recordingFile?.delete()
        super.onCleared()
    }
}
