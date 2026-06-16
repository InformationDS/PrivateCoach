# PrivateCoach 2.0 — AI-First 系统架构设计文档 v1.0

> 日期：2026-06-16  
> 依赖产品文档：`PRD_AI_FIRST_V1.0.md`  
> 核心约束：AI 能力全部由外部 Gemini API 提供，app 侧不做模型推理  
> 架构原则：在现有 Clean Architecture + MVVM 基础上增量演进，最大化复用

---

## 1. 架构总览

### 1.1 分层架构（不变）

```
┌──────────────────────────────────────────┐
│  UI Layer  (Compose + ViewModel)          │
│  - ConversationScreen (🆕)               │
│  - 现有 10 个 Screen (保留)               │
├──────────────────────────────────────────┤
│  Domain Layer  (Repository 接口)          │
│  - WorkoutRepository / TemplateRepository │
│  - SettingsRepository                     │
├──────────────────────────────────────────┤
│  Data Layer  (Room + DataStore + OkHttp)  │
│  - 5 个 DAO，DataStore，Repository Impl   │
│  - GeminiApiServiceImpl (扩展)            │
└──────────────────────────────────────────┘
```

### 1.2 核心设计决策

| 决策 | 结论 |
|------|------|
| AI 能力边界 | 外部 Gemini API 负责所有 NLP（解析、建议、报告）；App 侧仅做规则引擎 + 本地查询 |
| 对话持久化 | **不持久化**。消息列表仅存在于 ConversationViewModel 内存中 |
| 长期记忆 | Room 数据库中的训练数据 = AI 的记忆来源。每次会话冷启动时查询构建 context |
| 意图路由 | 本地规则引擎（关键词 + 正则），零延迟、零成本 |
| 流式响应 | 不做。全部一次性请求/返回 |
| 现有代码处置 | 增量演进：数据层不动，现有 Screen 保留，导航重构 |

---

## 2. 模块全景图

### 2.1 新增模块 vs 复用模块

```
com.privatecoach.app/
│
├── core/
│   ├── ai/                              ← AI 相关 (扩展)
│   │   ├── AiApiService.kt             ← 🔄 接口扩展 (新增 advice, review 方法)
│   │   ├── GeminiApiServiceImpl.kt     ← 🔄 实现扩展
│   │   ├── AiPromptBuilder.kt          ← 🔄 大幅扩展 (新增 advice/review/context prompts)
│   │   ├── AiResponseParser.kt         ← 复用
│   │   ├── IntentRouter.kt             ← 🆕 意图规则引擎
│   │   └── SessionContextBuilder.kt    ← 🆕 会话 context 构建器
│   │
│   ├── query/                           ← 🆕 本地查询引擎
│   │   └── QueryEngine.kt              ← 🆕 简单查询本地 SQL 执行 + 模板回复
│   │
│   ├── model/                           ← 模型 (扩展)
│   │   ├── AiModels.kt                 ← 🔄 扩展：新增 AdviceResult, ReviewResult
│   │   ├── ConversationModels.kt       ← 🆕 消息模型、意图枚举、会话状态
│   │   └── (其他现有模型不变)
│   │
│   ├── analytics/                       ← 复用 (TrendAnalyzer, VolumeCalculator, ReportGenerator)
│   ├── audio/                           ← 复用 (AudioRecorder, AudioPlayer)
│   └── security/                        ← 复用 (KeyStoreManager)
│
├── data/                                ← 数据层 (基本不变)
│   ├── local/                           ← Room 5 表不变, 5 DAO 不变
│   ├── repository/                      ← Repository 实现不变
│   ├── datastore/                       ← SettingsDataStore 不变
│   └── mapper/                          ← 不变
│
├── domain/                              ← 领域层 (不变)
│   └── repository/                      ← 3 个 Repository 接口不变
│
├── di/                                  ← DI (扩展)
│   ├── AiModule.kt                     ← 🔄 新增 IntentRouter, SessionContextBuilder 绑定
│   ├── QueryModule.kt                  ← 🆕 QueryEngine 绑定
│   └── (其他 4 个 Module 不变)
│
└── ui/                                  ← UI 层 (大幅扩展)
    ├── screen/
    │   ├── conversation/                ← 🆕 对话主界面
    │   │   ├── ConversationScreen.kt
    │   │   ├── ConversationViewModel.kt
    │   │   ├── components/
    │   │   │   ├── MessageList.kt
    │   │   │   ├── WelcomeHeader.kt
    │   │   │   ├── QuickActionChips.kt
    │   │   │   ├── ChatInputBar.kt
    │   │   │   └── VoiceRecordButton.kt
    │   │   └── bubbles/
    │   │       ├── UserTextBubble.kt
    │   │       ├── UserVoiceBubble.kt
    │   │       ├── AiTextBubble.kt
    │   │       ├── ConfirmCard.kt
    │   │       ├── DataCard.kt
    │   │       ├── ChartCard.kt
    │   │       ├── AdviceCard.kt
    │   │       └── SummaryCard.kt
    │   ├── dashboard/                   ← 保留 (汉堡菜单入口)
    │   ├── calendar/                    ← 保留 (底部 Tab 3)
    │   ├── analysis/                    ← 保留 (汉堡菜单入口)
    │   ├── history/                     ← 保留 (底部 Tab 2)
    │   ├── detail/                      ← 保留 (跳转进入)
    │   ├── record/                      ← 🗑️ 降级 (ManualEntryScreen 保留作为深度编辑兜底)
    │   ├── template/                    ← 保留 (汉堡菜单入口)
    │   └── settings/                    ← 保留 (汉堡菜单入口)
    ├── navigation/
    │   ├── Screen.kt                   ← 🔄 调整路由表
    │   ├── BottomNavItem.kt            ← 🔄 3 Tab 改为 (AI助手 | 记录 | 日历)
    │   └── PrivateCoachNavHost.kt      ← 🔄 导航宿主重构
    └── component/                       ← 复用 + 新增对话专用组件
```

---

## 3. 核心数据流

### 3.1 总流程

```
┌────────────┐    ┌──────────────┐    ┌─────────────────┐
│ 用户输入    │───▶│ IntentRouter │───▶│ 路由到对应处理链  │
│ (文本/语音) │    │ (本地规则引擎)│    └──────┬──────────┘
└────────────┘    └──────────────┘           │
                              ┌────────┬─────┼─────┬──────┐
                              ▼        ▼     ▼     ▼      ▼
                           RECORD   QUERY  ADVICE REVIEW MANAGE
```

### 3.2 记录类流程 (RECORD)

```
用户文本输入 (或语音转文字后)
    │
    ▼
IntentRouter.classify() → Intent.RECORD
    │
    ▼
ConversationViewModel.handleRecordIntent()
    │
    ├── 添加 UserTextBubble / UserVoiceBubble 到消息列表
    │
    ├── 调用 aiApiService.parseText(text)
    │   或 aiApiService.transcribeAndParse(audioFile)
    │   ─────────────────────────────────────────
    │   │ Gemini API (外部)                      │
    │   │ System prompt = 解析规则 (AiPromptBuilder)│
    │   │ 返回 JSON → AiParsedResult             │
    │   ─────────────────────────────────────────
    │
    ├── 检查同日是否已有 Workout
    │   ├── 有 → 展示追加/覆盖选择卡片
    │   └── 无 → 展示 ConfirmCard
    │
    └── 等待用户交互
        ├── [确认保存] → workoutRepository.saveWorkout() → 成功消息
        ├── [编辑]     → 内联编辑 / 跳转 ManualEntryScreen
        ├── [追加文本] → 回到 parseText → 增量更新确认卡
        └── [取消]     → 移除确认卡
```

### 3.3 查询类流程 (QUERY)

```
用户输入
    │
    ▼
IntentRouter.classify() → Intent.QUERY
    │
    ▼
IntentRouter.extractEntities() 
    → { timeRange: "本周", exercise: null, metric: "训练天数" }
    │
    ▼
QueryEngine.execute(intent, entities)
    │
    ├── 简单查询 (训练天数、最近训练等)
    │   └── 纯本地 SQL → 模板拼回复文本
    │       → 渲染 AiTextBubble / DataCard
    │
    └── 复杂查询 (趋势、对比)
        ├── 本地数据查询 + 聚合
        ├── 数据注入 Gemini prompt
        ─────────────────────────────
        │ Gemini API (外部)           │
        │ 解读数据 → 自然语言文本     │
        ─────────────────────────────
        └── 渲染 AiTextBubble + ChartCard (Canvas 图表)
```

### 3.4 建议类流程 (ADVICE)

```
用户输入: "卧推卡在60kg好久了"
    │
    ▼
IntentRouter.classify() → Intent.ADVICE
    │
    ▼
IntentRouter.extractEntities()
    → { exercise: "平板卧推", concern: "平台期" }
    │
    ▼
ConversationViewModel.handleAdviceIntent()
    │
    ├── 查询详细数据 (12周趋势、频率、辅助动作等)
    │   ├── workoutRepository.getExerciseTrendData("平板卧推")
    │   ├── workoutRepository.getBodyPartDistribution(...)
    │   └── workoutRepository.getTrainingFrequency(...)
    │
    ├── 聚合为 AdviceContext 数据结构
    │
    ├── 调用 aiApiService.generateAdvice(adviceContext)
    │   ─────────────────────────────────────────────
    │   │ Gemini API (外部)                          │
    │   │ System prompt = 训练科学知识 + 数据 context  │
    │   │ + "你是资深健身教练，基于数据给出建议"      │
    │   │ 返回结构化 JSON (AdviceResult)              │
    │   ─────────────────────────────────────────────
    │
    └── 渲染 AdviceCard
        ├── 数据摘要区
        ├── 分析区
        ├── 建议区
        └── [有帮助] [不相关] 反馈按钮
```

### 3.5 复盘类流程 (REVIEW)

```
用户输入: "总结这周"
    │
    ▼
IntentRouter.classify() → Intent.REVIEW
    │
    ▼
本地数据聚合:
    ├── trainingDays = workoutRepository.getTrainingDaysCount(本周)
    ├── volumeData = workoutRepository.getVolumeData(本周)
    ├── bodyParts = workoutRepository.getBodyPartDistribution(本周)
    └── 上一周期对比数据
    │
    ▼
调用 aiApiService.generateReview(reviewContext)
    ─────────────────────────────────────
    │ Gemini API (外部)                  │
    │ 生成结构化周报/月报                │
    │ 返回 ReviewResult                  │
    ─────────────────────────────────────
    │
    ▼
渲染 SummaryCard (含数据 + Canvas 图表)
```

---

## 4. 核心组件详细设计

### 4.1 AiApiService — 扩展后的接口

```kotlin
// core/ai/AiApiService.kt

interface AiApiService {
    // === 已有方法 ===
    suspend fun transcribeAndParse(audioFile: File): Result<AiParsedResult>
    suspend fun parseText(text: String): Result<AiParsedResult>
    suspend fun generateReport(reportData: ReportInputData): Result<String>

    // === 🆕 新增方法 ===
    
    /** 生成训练建议 (ADVICE intent) */
    suspend fun generateAdvice(context: AdviceContext): Result<AdviceResult>
    
    /** 生成运营复盘 (REVIEW intent) */
    suspend fun generateReview(context: ReviewContext): Result<ReviewResult>
    
    /** 简单查询的 AI 解读 (复杂 QUERY 时使用) */
    suspend fun interpretQuery(context: QueryContext): Result<String>
}
```

### 4.2 IntentRouter — 意图规则引擎

```kotlin
// core/ai/IntentRouter.kt

@Singleton
class IntentRouter @Inject constructor() {

    fun classify(input: String): Intent {
        return when {
            // 记录类关键词
            input.containsAny("练了", "做了", "今天", "刚.*[做练]", "记录",
                              "加.*组", "追加", "再加", "跑了", "游了", "骑了") -> Intent.RECORD
            
            // 建议类关键词
            input.containsAny("建议", "怎么办", "卡住", "平台期", "规划", 
                              "推荐", "帮.*看", "分析", "怎么.*练", "该练什么",
                              "明天练", "下次练") -> Intent.ADVICE
            
            // 复盘类关键词 
            input.containsAny("总结", "复盘", "周报", "月报", "回顾",
                              "怎么样$", "如何$") -> Intent.REVIEW
            
            // 操作类关键词
            input.containsAny("删除", "删了", "保存为模板", "存为模板") -> Intent.MANAGE
            
            // 查询类关键词
            input.containsAny("几次", "多少", "什么.*时候", "趋势", "对比", 
                              "进步", "变化", "看一下", "查", "哪天", "哪个",
                              "几组", "多重", "容量", "频率", "分布") -> Intent.QUERY
            
            // 模糊 → 追问
            input.length < 5 && !input.contains(Regex("\\d")) -> Intent.AMBIGUOUS
            
            // 默认 → 尝试记录解析
            else -> Intent.RECORD
        }
    }
    
    fun extractEntities(input: String, intent: Intent): Map<String, String> {
        // 提取：动作名、时间范围、部位、指标类型等
    }
    
    // 安全边界判断
    fun checkSafety(input: String): SafetyVerdict {
        // HIGH_RISK → 拒答 + 引导
        // IRRELEVANT → 拒答 + 引导回健身
        // FORBIDDEN → 严禁操作
        // OK → 正常处理
    }
}

sealed class Intent {
    data object RECORD : Intent()
    data object QUERY : Intent()
    data object ADVICE : Intent()
    data object REVIEW : Intent()
    data object MANAGE : Intent()
    data object AMBIGUOUS : Intent()
}
```

### 4.3 QueryEngine — 本地查询引擎

```kotlin
// core/query/QueryEngine.kt

@Singleton
class QueryEngine @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * 执行本地查询。
     * 简单查询直接返回结果文本，复杂查询返回数据 context 供 AI 解读。
     */
    suspend fun execute(
        intent: Intent.QUERY,
        entities: Map<String, String>
    ): QueryResult {
        val timeRange = parseTimeRange(entities["timeRange"])
        val exercise = entities["exercise"]
        val metric = entities["metric"]
        
        return when {
            // 简单计数 → 纯本地
            metric == "训练天数" -> {
                val days = workoutRepository.getTrainingDaysCount(timeRange.start, timeRange.end)
                QueryResult.LocalText("${timeRange.label}训练了 ${days} 天")
            }
            
            // 趋势查询 → 数据 + 图表
            exercise != null && metric == "趋势" -> {
                val data = workoutRepository.getExerciseTrendData(exercise)
                QueryResult.ChartData(data, ChartType.LINE, "最大重量趋势")
            }
            
            // 部位分布 → 数据 + 饼图
            metric == "部位分布" -> {
                val data = workoutRepository.getBodyPartDistribution(timeRange.start, timeRange.end)
                QueryResult.ChartData(data, ChartType.PIE, "训练部位分布")
            }
            
            // 复杂对比 → 数据聚合，交给 AI 解读
            metric == "对比" -> {
                val current = aggregatePeriodData(timeRange)
                val previous = aggregatePeriodData(timeRange.previous())
                QueryResult.AiContext(QueryContext(current, previous, timeRange))
            }
            
            else -> QueryResult.LocalText("未识别的查询，请换个方式问我")
        }
    }
}

sealed class QueryResult {
    data class LocalText(val text: String) : QueryResult()
    data class ChartData(val data: Any, val chartType: ChartType, val title: String) : QueryResult()
    data class AiContext(val context: QueryContext) : QueryResult()
}
```

### 4.4 SessionContextBuilder — 会话初始化 Context

```kotlin
// core/ai/SessionContextBuilder.kt

@Singleton
class SessionContextBuilder @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val trendAnalyzer: TrendAnalyzer,
    private val volumeCalculator: VolumeCalculator
) {
    /**
     * 每次新会话开始时调用一次。
     * 查询用户训练画像，构建注入 AI system prompt 的 context 文本。
     */
    suspend fun build(): SessionContext {
        val now = LocalDate.now()
        val weekStart = now.with(DayOfWeek.MONDAY)
        val fourWeeksAgo = now.minusWeeks(4)
        
        // 并行查询
        val trainingDaysThisWeek = workoutRepository.getTrainingDaysCount(weekStart, now)
        val recentWorkout = workoutRepository.getMostRecentWorkoutOnce()
        val allWorkouts = workoutRepository.getAllWorkoutsOnce()
        val bodyParts = workoutRepository.getBodyPartDistribution(weekStart, now)
        val frequency = workoutRepository.getTrainingFrequency(fourWeeksAgo, now)
        
        // 计算本周部位覆盖
        val coveredParts = bodyParts.map { it.bodyPart }.toSet()
        val uncoveredParts = BodyPart.entries.filter { 
            it != BodyPart.FULL_BODY && it.name !in coveredParts 
        }
        
        // 检测停滞动作 (>4周无进步)
        val stagnatingExercises = detectStagnation(allWorkouts, fourWeeksAgo)
        
        return SessionContext(
            trainingDaysThisWeek = trainingDaysThisWeek,
            recentWorkoutSummary = recentWorkout?.toSummary(),
            coveredBodyParts = coveredParts,
            uncoveredBodyParts = uncoveredParts,
            stagnatingExercises = stagnatingExercises,
            frequentExercises = extractFrequentExercises(allWorkouts),
            // ... 
        )
    }
}
```

### 4.5 ConversationViewModel — 对话状态管理

```kotlin
// ui/screen/conversation/ConversationViewModel.kt

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: TemplateRepository,
    private val aiApiService: AiApiService,
    private val intentRouter: IntentRouter,
    private val queryEngine: QueryEngine,
    private val sessionContextBuilder: SessionContextBuilder,
    private val audioRecorder: AudioRecorder
) : ViewModel() {

    // ── 状态 ──
    
    /** 消息列表 (仅内存，不持久化) */
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    /** 对话状态 */
    private val _state = MutableStateFlow(ConversationState.IDLE)
    val state: StateFlow<ConversationState> = _state.asStateFlow()
    
    /** 当前确认卡数据 (非 null 时 UI 展示确认卡) */
    private val _pendingConfirm = MutableStateFlow<PendingConfirmData?>(null)
    val pendingConfirm: StateFlow<PendingConfirmData?> = _pendingConfirm.asStateFlow()
    
    /** 会话 context (初始化时加载一次) */
    private var sessionContext: SessionContext? = null
    
    // ── 公开方法 ──
    
    fun initialize() {
        viewModelScope.launch {
            sessionContext = sessionContextBuilder.build()
            showWelcomeMessage(sessionContext!!)
        }
    }
    
    fun handleTextInput(text: String) {
        viewModelScope.launch {
            // 1. 安全检查
            when (val safety = intentRouter.checkSafety(text)) {
                SafetyVerdict.HIGH_RISK -> { showHighRiskRejection(); return@launch }
                SafetyVerdict.IRRELEVANT -> { showIrrelevantRejection(); return@launch }
                SafetyVerdict.FORBIDDEN -> { showForbiddenRejection(); return@launch }
                SafetyVerdict.OK -> {}
            }
            
            // 2. 添加用户消息
            addMessage(UserTextMessage(text))
            
            // 3. 意图路由
            when (intentRouter.classify(text)) {
                Intent.RECORD    -> handleRecord(text)
                Intent.QUERY     -> handleQuery(text)
                Intent.ADVICE    -> handleAdvice(text)
                Intent.REVIEW    -> handleReview(text)
                Intent.MANAGE    -> handleManage(text)
                Intent.AMBIGUOUS -> handleAmbiguous(text)
            }
        }
    }
    
    fun handleVoiceInput(audioFile: File) {
        // 类似文本流程，但走 AiApiService.transcribeAndParse()
    }
    
    fun confirmWorkout() { /* 保存到 Room */ }
    fun editWorkout()    { /* 内联编辑 / 跳转 */ }
    fun cancelWorkout()  { /* 移除确认卡 */ }
    fun appendToWorkout(text: String) { /* 增量追加 */ }
}
```

### 4.6 消息模型

```kotlin
// core/model/ConversationModels.kt

sealed class Message(val id: String, val timestamp: Instant) {
    // 用户消息
    data class UserText(val content: String) : Message(...)
    data class UserVoice(val audioFile: File, val transcript: String) : Message(...)
    
    // AI 消息
    data class AiText(val markdown: String) : Message(...)
    data class ConfirmCard(val data: AiParsedResult, val isAppend: Boolean) : Message(...)
    data class DataCard(val title: String, val stats: List<StatItem>, val chartData: ChartData?) : Message(...)
    data class ChartCard(val title: String, val chartData: ChartData, val interpretation: String) : Message(...)
    data class AdviceCard(val result: AdviceResult) : Message(...)
    data class SummaryCard(val result: ReviewResult) : Message(...)
    data class QuickActions(val chips: List<QuickActionChip>) : Message(...)
    data class SystemMessage(val text: String, val level: SystemMsgLevel) : Message(...)
}

enum class ConversationState { IDLE, LOADING, RECORDING, AWAITING_CONFIRMATION }

data class PendingConfirmData(
    val parsedResult: AiParsedResult,
    val existingWorkoutId: Long?,  // null = 新记录, non-null = 追加/覆盖
    val sourceText: String         // AI 听到/读到的原文
)
```

---

## 5. Gemini API 交互设计

### 5.1 API 调用全景

```
外部 Gemini API (GeminiApiServiceImpl)
│
├── transcribeAndParse(audioFile)     ← RECORD (语音)
│   Input:  音频文件 (base64) + 解析 system prompt
│   Output: AiParsedResult (结构化 JSON)
│
├── parseText(text)                   ← RECORD (文字)
│   Input:  用户文本 + 解析 system prompt
│   Output: AiParsedResult (结构化 JSON)
│
├── generateAdvice(context)           ← ADVICE
│   Input:  建议 system prompt + 数据 context JSON
│   Output: AdviceResult (结构化 JSON)
│
├── generateReview(context)           ← REVIEW
│   Input:  复盘 system prompt + 数据 context JSON
│   Output: ReviewResult (结构化 JSON)
│
├── interpretQuery(context)           ← QUERY (复杂)
│   Input:  查询 system prompt + 数据 context JSON
│   Output: String (自然语言解读)
│
└── generateReport(data)              ← 复用 (分析页报告)
```

### 5.2 新增 Prompt 结构

**建议 Prompt (AiPromptBuilder.buildAdvicePrompt)**:
```
System:
你是资深健身教练，拥有运动科学背景。用户是个人健身爱好者。

你需要基于以下训练数据给出个性化建议：

训练数据：
{adviceContextJson}

规则：
- 建议必须引用数据中的具体数字
- 平台期分析要检查频率、容量、辅助动作、渐进超负荷四个维度
- 避免医疗诊断，涉及伤病风险时提示寻求专业评估
- 输出 JSON 格式，包含 dataSummary、analysis、suggestions 三个部分
- 数据不足时在 analysis 中明确说明
```

**复盘 Prompt (AiPromptBuilder.buildReviewPrompt)**:
```
System:
你是健身数据分析师。基于训练数据生成周报/月报。

训练数据：
{reviewContextJson}

输出 JSON，包含：
- overview: 整体概览文本
- highlights: 亮点列表
- concerns: 需关注的问题
- bodyPartDistribution: 部位分布数据
- comparison: 与上一周期对比
```

### 5.3 API 调用开销分析

| 意图 | 是否调 API | API 调用次数 | 预估延迟 |
|------|-----------|-------------|---------|
| RECORD (文字) | ✅ | 1次 parseText | 2-4s |
| RECORD (语音) | ✅ | 1次 transcribeAndParse | 3-5s |
| QUERY (简单) | ❌ | 0 (纯本地) | <100ms |
| QUERY (复杂) | ✅ | 1次 interpretQuery | 2-3s |
| ADVICE | ✅ | 1次 generateAdvice | 3-8s |
| REVIEW | ✅ | 1次 generateReview | 2-5s |
| MANAGE | ❌ | 0 (纯本地) | <100ms |

---

## 6. 导航重构

### 6.1 新路由表

```kotlin
// ui/navigation/Screen.kt

sealed interface Screen {
    // ── 底部 Tab ──
    @Serializable data object Conversation : Screen   // 🆕 AI对话 (默认首页)
    @Serializable data object History : Screen         // 保留 (训练记录)
    @Serializable data object Calendar : Screen        // 保留 (日历)
    
    // ── 汉堡菜单 ──
    @Serializable data object Dashboard : Screen       // 保留 (降级为菜单入口)
    @Serializable data object Analysis : Screen        // 保留
    @Serializable data object TemplateList : Screen    // 保留
    @Serializable data object Settings : Screen        // 保留
    
    // ── 全屏跳转 ──
    @Serializable data class WorkoutDetail(val workoutId: Long) : Screen
    @Serializable data class TemplateEdit(val templateId: Long? = null) : Screen
    @Serializable data class ManualEntry(val templateId: Long? = null) : Screen  // 保留作为兜底
    
    // ── 🗑️ 移除的路由 ──
    // Record, ConfirmResult → 被 Conversation 对话化替代
}
```

### 6.2 新底部导航

```kotlin
// ui/navigation/BottomNavItem.kt

enum class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector) {
    CONVERSATION(Screen.Conversation, "AI助手", Icons.Outlined.Chat),
    HISTORY(Screen.History, "训练记录", Icons.Outlined.ListAlt),
    CALENDAR(Screen.Calendar, "日历", Icons.Outlined.DateRange)
}
```

### 6.3 导航容器重构

```
Scaffold
├── TopBar (仅在汉堡菜单页面显示)
│   └── 汉堡按钮 → DrawerState.open()
│
├── BottomBar (3 Tab: AI助手 | 训练记录 | 日历)
│
├── ModalDrawer (汉堡菜单内容)
│   ├── 📊 仪表盘
│   ├── 📈 分析报告  
│   ├── 📝 模板管理
│   └── ⚙️ 设置
│
└── NavHost
    ├── Conversation (startDestination)
    ├── History
    ├── Calendar
    ├── Dashboard
    ├── Analysis
    ├── TemplateList
    ├── Settings
    ├── WorkoutDetail
    ├── TemplateEdit
    └── ManualEntry
```

---

## 7. 依赖注入

### 7.1 新增 DI Module

```kotlin
// di/QueryModule.kt (🆕)
@Module
@InstallIn(SingletonComponent::class)
object QueryModule {
    @Provides @Singleton
    fun provideQueryEngine(
        workoutRepository: WorkoutRepository
    ): QueryEngine = QueryEngine(workoutRepository)
}

// di/AiModule.kt (🔄 扩展)  
@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds @Singleton
    abstract fun bindAiApiService(impl: GeminiApiServiceImpl): AiApiService
    
    // 🆕
    @Binds @Singleton  
    abstract fun bindIntentRouter(impl: IntentRouter): IntentRouter
    
    @Binds @Singleton
    abstract fun bindSessionContextBuilder(impl: SessionContextBuilder): SessionContextBuilder
}
```

---

## 8. 安全架构

### 8.1 安全层级

```
用户输入
    │
    ▼
IntentRouter.checkSafety()
    │
    ├── FORBIDDEN (严禁操作)
    │   └── 拒答，不生成确认卡，引导到手动页面
    │
    ├── HIGH_RISK (医疗/伤病)
    │   └── 拒答，引导就医，可帮备注
    │
    ├── IRRELEVANT (无关问题)
    │   └── 拒答，引导回健身场景
    │
    └── OK
        │
        ▼
    继续业务处理
        │
        ├── 只读 → 直接返回
        └── 写入 → 必须通过 ConfirmCard → 用户点击确认 → Repository 写入
```

### 8.2 严禁操作清单

以下操作由 `IntentRouter.checkSafety()` 在路由前拦截：

| 操作 | 拦截方式 | fallback |
|------|---------|----------|
| 批量删除 | 关键词匹配 + 二次确认 | 引导到 History 页手动删除 |
| 清空数据 | 关键词匹配 | 拒绝 + 引导到设置页导出 |
| 修改 API 配置 | 关键词匹配 | 引导到设置页 |
| 医疗诊断请求 | 关键词 + 伤病词匹配 | 拒答 + 引导就医 |

---

## 9. 降级策略

```
AI 可用?
  ├── YES → 完整功能
  └── NO  (网络断开 / API Key 未配置 / API 返回错误)
      │
      ├── ConversationScreen
      │   ├── 显示降级横幅："AI 暂不可用"
      │   ├── 快捷指令隐藏"记录训练"
      │   ├── 保留"查看进度"类纯本地快捷指令
      │   └── 输入区提示："AI 暂不可用，可前往训练记录页手动录入"
      │
      ├── History / Calendar / Analysis / Dashboard
      │   └── 完整可用 (纯本地数据)
      │
      └── Settings → ManualEntry
          └── 手动录入兜底入口
```

降级判断逻辑：
```kotlin
// ConversationViewModel 中
private suspend fun isAiAvailable(): Boolean {
    val apiKey = settingsRepository.apiKey.first()
    if (apiKey.isBlank()) return false
    // 可选：做一次轻量 health check
    return true
}
```

---

## 10. 数据模型扩展

### 10.1 新增模型

```kotlin
// core/model/ConversationModels.kt

// 会话初始化 Context
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

// 建议请求 Context
data class AdviceContext(
    val exerciseName: String,
    val concern: String,
    val trendData: List<ExerciseTrendPoint>,
    val frequencyData: List<TrainingFrequencyPoint>,
    val relatedExercises: List<String>,
    val bodyPartDistribution: List<BodyPartCount>,
    val sessionContext: SessionContext
)

// 建议结果
data class AdviceResult(
    val dataSummary: String,
    val analysis: String,
    val suggestions: List<String>,
    val dataSufficiency: DataSufficiency  // SUFFICIENT | LIMITED | INSUFFICIENT
)

// 复盘结果
data class ReviewResult(
    val overview: String,
    val highlights: List<String>,
    val concerns: List<String>,
    val bodyPartDistribution: List<BodyPartCount>,
    val comparison: PeriodComparison?
)
```

### 10.2 实体层变更

**无需新增 Room 表**。现有 5 张表完全够用。

---

## 11. 文件清单

### 11.1 新增文件 (~25 个)

```
core/
├── ai/
│   └── IntentRouter.kt              🆕 ~120 行 (规则引擎)
│   └── SessionContextBuilder.kt     🆕 ~150 行 (context 构建)
├── query/
│   └── QueryEngine.kt               🆕 ~200 行 (本地查询引擎)
└── model/
    └── ConversationModels.kt        🆕 ~120 行 (消息/意图/会话模型)

ui/screen/conversation/
├── ConversationScreen.kt            🆕 ~250 行 (主界面)
├── ConversationViewModel.kt         🆕 ~500 行 (核心 ViewModel)
└── components/
    ├── MessageList.kt               🆕 ~80 行
    ├── WelcomeHeader.kt             🆕 ~120 行 (动态今日摘要)
    ├── QuickActionChips.kt          🆕 ~80 行
    ├── ChatInputBar.kt              🆕 ~150 行
    ├── VoiceRecordButton.kt         🆕 ~200 行 (复用 AudioRecorder)
    └── bubbles/
        ├── UserTextBubble.kt        🆕 ~50 行
        ├── UserVoiceBubble.kt       🆕 ~80 行
        ├── AiTextBubble.kt          🆕 ~50 行
        ├── ConfirmCard.kt           🆕 ~350 行 (核心交互组件)
        ├── DataCard.kt              🆕 ~120 行
        ├── ChartCard.kt             🆕 ~100 行 (复用 PcLineChart/PcBarChart/PcPieChart)
        ├── AdviceCard.kt            🆕 ~180 行
        └── SummaryCard.kt           🆕 ~150 行

di/
└── QueryModule.kt                   🆕 ~15 行
```

### 11.2 修改文件 (~8 个)

```
core/ai/
├── AiApiService.kt                  🔄 +3 方法
├── AiPromptBuilder.kt               🔄 +3 prompt builder
├── GeminiApiServiceImpl.kt          🔄 +3 方法实现
└── AiResponseParser.kt              🔄 +2 解析方法

ui/navigation/
├── Screen.kt                        🔄 路由表调整 (+Conversation, -Record, -ConfirmResult)
├── BottomNavItem.kt                 🔄 5→3 Tab
└── PrivateCoachNavHost.kt           🔄 导航宿主重构

di/
└── AiModule.kt                      🔄 +IntentRouter, +SessionContextBuilder
```

### 11.3 删除/降级文件

```
ui/screen/record/
├── RecordScreen.kt                  🗑️ 删除 (功能由 ConversationScreen 替代)
└── ConfirmResultScreen.kt           🗑️ 删除 (功能由 ConfirmCard 替代)
```

---

## 12. 构建顺序

按依赖关系分层施工，每个检查点可独立验证：

```
Checkpoint 1: 数据模型 + AI 接口扩展 (底层基础)
├── 🆕 ConversationModels.kt
├── 🔄 AiModels.kt (新增 AdviceResult, ReviewResult 等)
├── 🔄 AiApiService.kt (接口新增方法)
└── 🔄 AiPromptBuilder.kt (新增 prompt)

Checkpoint 2: 核心引擎 (业务逻辑层)
├── 🆕 IntentRouter.kt
├── 🆕 QueryEngine.kt
├── 🆕 SessionContextBuilder.kt
├── 🔄 GeminiApiServiceImpl.kt (实现新增方法)
└── 🔄 AiResponseParser.kt (新增解析)

Checkpoint 3: 对话 UI + Bubble 组件
├── 🆕 MessageList.kt
├── 🆕 WelcomeHeader.kt
├── 🆕 QuickActionChips.kt
├── 🆕 ChatInputBar.kt
├── 🆕 VoiceRecordButton.kt
└── 🆕 全部 8 个 Bubble 组件

Checkpoint 4: ConversationViewModel
└── 🆕 ConversationViewModel.kt (聚合所有引擎 + 状态管理)

Checkpoint 5: ConversationScreen + 导航重构
├── 🆕 ConversationScreen.kt
├── 🔄 Screen.kt
├── 🔄 BottomNavItem.kt
├── 🔄 PrivateCoachNavHost.kt (3 Tab + Drawer)
├── 🔄 AiModule.kt
└── 🆕 QueryModule.kt

Checkpoint 6: 清理 + 降级
├── 🗑️ RecordScreen.kt
├── 🗑️ ConfirmResultScreen.kt
├── 🔄 降级 UI 提示
└── 🔄 ManualEntryScreen 作为兜底保留
```

---

## 13. 关键技术决策记录

| # | 决策 | 理由 |
|---|------|------|
| 1 | 意图路由用本地规则引擎，不用 AI 做意图分类 | 零延迟、零成本；记录/查询/建议三大意图关键词区分度足够 |
| 2 | 简单查询纯本地 SQL + 模板，不调 AI | 省 API 成本、响应即时；"这周练了几次"不需要 AI |
| 3 | 对话消息不持久化 | 产品决策；避免新增 Room 表和迁移复杂度 |
| 4 | 不复用现有 RecordScreen/ConfirmResultScreen | 对话页确认卡内联编辑体验更好，不需要页面跳转 |
| 5 | 保留 ManualEntryScreen 作为兜底 | AI 不可用或用户想手动填表时，仍是合法路径 |
| 6 | 确认卡增量更新（追加不改重建） | 避免每次追加都重新调 AI，省成本 + 响应快 |
| 7 | 不做流式响应 | 产品决策；简化实现，一次性返回在当前场景足够 |
| 8 | 不新增 Room 表 | 对话不持久化，现有 5 表覆盖所有训练数据 |
| 9 | Drawer 而非顶部下拉菜单 | 参数量多时体验更好，且兼容 Material 3 标准组件 |
