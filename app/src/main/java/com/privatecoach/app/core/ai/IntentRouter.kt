package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.ClassifiedIntent
import com.privatecoach.app.core.model.IntentType
import com.privatecoach.app.core.model.SafetyVerdict
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rule-based intent classification engine.
 * Uses keyword/pattern matching — zero latency, zero API cost.
 */
@Singleton
class IntentRouter @Inject constructor() {

    // ═══════════════════════════════════════════
    // Intent Classification
    // ═══════════════════════════════════════════

    fun classify(input: String): ClassifiedIntent {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            return ClassifiedIntent(IntentType.AMBIGUOUS, 0.5f)
        }

        // ADVICE: suggestion, plateau, planning keywords
        if (trimmed.matchesAdvice()) {
            return ClassifiedIntent(IntentType.ADVICE, 0.9f, extractAdviceEntities(trimmed))
        }

        // REVIEW: summary, recap keywords
        if (trimmed.matchesReview()) {
            return ClassifiedIntent(IntentType.REVIEW, 0.9f, extractReviewEntities(trimmed))
        }

        // MANAGE: delete, template operations
        if (trimmed.matchesManage()) {
            return ClassifiedIntent(IntentType.MANAGE, 0.9f, extractManageEntities(trimmed))
        }

        // QUERY: question about past data
        if (trimmed.matchesQuery()) {
            return ClassifiedIntent(IntentType.QUERY, 0.85f, extractQueryEntities(trimmed))
        }

        // RECORD: training logging
        if (trimmed.matchesRecord()) {
            return ClassifiedIntent(IntentType.RECORD, 0.85f, extractRecordEntities(trimmed))
        }

        // AMBIGUOUS: too short, unclear intent
        if (trimmed.length < 5 && !trimmed.contains(Regex("\\d"))) {
            return ClassifiedIntent(IntentType.AMBIGUOUS, 0.3f)
        }

        // Default: try RECORD (contains digits/weight-like patterns → probably training log)
        return ClassifiedIntent(IntentType.RECORD, 0.5f)
    }

    // ═══════════════════════════════════════════
    // Safety Boundary Check
    // ═══════════════════════════════════════════

    fun checkSafety(input: String): SafetyVerdict {
        // FORBIDDEN: batch delete, data destruction
        if (input.contains(Regex("删除所有|清空|全部删|批量删|删光|重置"))) {
            return SafetyVerdict.FORBIDDEN
        }

        // FORBIDDEN: API config manipulation
        if (input.contains(Regex("修改.*API|改.*密钥|换.*模型|改.*配置|修改.*设置"))) {
            return SafetyVerdict.FORBIDDEN
        }

        // HIGH_RISK: medical/injury questions
        if (input.contains(Regex("膝盖.*疼|腰.*疼|肩膀.*疼|受伤|扭伤|拉伤|发炎|刺痛|麻了|胸口疼|头晕|恶心"))
        ) {
            return SafetyVerdict.HIGH_RISK
        }

        // IRRELEVANT: off-topic
        if (input.contains(Regex(
                "诗|电影|新闻|天气|股票|代码|Python|Java|写.*文章|写.*小说|推荐.*电影|推荐.*书|国际|政治"
            ))
        ) {
            return SafetyVerdict.IRRELEVANT
        }

        return SafetyVerdict.OK
    }

    // ═══════════════════════════════════════════
    // Entity Extraction
    // ═══════════════════════════════════════════

    fun extractEntities(input: String, intentType: IntentType): Map<String, String> {
        return when (intentType) {
            IntentType.RECORD -> extractRecordEntities(input)
            IntentType.QUERY -> extractQueryEntities(input)
            IntentType.ADVICE -> extractAdviceEntities(input)
            IntentType.REVIEW -> extractReviewEntities(input)
            IntentType.MANAGE -> extractManageEntities(input)
            IntentType.AMBIGUOUS -> emptyMap()
        }
    }

    private fun extractRecordEntities(input: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()
        // Extract body part hints
        BodyPartHints.entries.forEach { hint ->
            if (input.contains(hint.keyword)) {
                entities["bodyPartHint"] = hint.bodyPartName
            }
        }
        // Extract template reference
        val templateMatch = Regex("按[「「](.+?)[」」]|[照按](.+?)模板").find(input)
        if (templateMatch != null) {
            entities["templateName"] = templateMatch.groupValues[1].ifEmpty {
                templateMatch.groupValues[2]
            }
        }
        return entities
    }

    private fun extractQueryEntities(input: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()

        // Time range
        when {
            input.contains("今天") || input.contains("今日") -> entities["timeRange"] = "today"
            input.contains("昨天") || input.contains("昨日") -> entities["timeRange"] = "yesterday"
            input.contains("本周") || input.contains("这周") || input.contains("这个星期") ->
                entities["timeRange"] = "this_week"
            input.contains("上周") || input.contains("上个星期") -> entities["timeRange"] = "last_week"
            input.contains("本月") || input.contains("这个月") -> entities["timeRange"] = "this_month"
            input.contains("上月") || input.contains("上个月") -> entities["timeRange"] = "last_month"
            input.contains("近.*月") || input.contains("最近.*月") -> {
                val monthMatch = Regex("(\\d+)\\s*个?月").find(input)
                entities["timeRange"] = if (monthMatch != null) "recent_${monthMatch.groupValues[1]}_months"
                else "this_month"
            }
            input.contains("全部") || input.contains("所有") || input.contains("总共") ->
                entities["timeRange"] = "all"
        }

        // Metric type
        when {
            input.contains("几次") || input.contains("多少天") || input.contains("几天") ->
                entities["metric"] = "training_days"
            input.contains("趋势") || input.contains("进步") || input.contains("变化") ->
                entities["metric"] = "trend"
            input.contains("容量") || input.contains("总量") || input.contains("总重量") ->
                entities["metric"] = "volume"
            input.contains("频率") -> entities["metric"] = "frequency"
            input.contains("分布") || input.contains("哪个部位") || input.contains("什么部位") ||
                input.contains("练最多") || input.contains("练最少") -> entities["metric"] = "distribution"
            input.contains("对比") || input.contains("比较") || input.contains("vs") ->
                entities["metric"] = "comparison"
        }

        // Exercise name (extract after action keywords)
        val exercisePattern = Regex("(?:卧推|深蹲|硬拉|划船|推举|飞鸟|弯举|臂屈伸|下拉|腿举|腿弯举|臀推|平板支撑|引体向上|双杠臂屈伸|跑步|游泳|骑行)")
        val matches = exercisePattern.findAll(input).toList()
        if (matches.isNotEmpty()) {
            entities["exercise"] = matches.last().value
        }

        // Compare intent
        if (input.contains("对比") || input.contains("比较") || input.contains("比") || input.contains("相比")) {
            entities["metric"] = "comparison"
        }

        return entities
    }

    private fun extractAdviceEntities(input: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()

        // Extract exercise name
        val exercisePattern = Regex(
            "(?:卧推|深蹲|硬拉|划船|推举|飞鸟|弯举|臂屈伸|下拉|腿举|腿弯举|臀推|平板支撑|引体向上|双杠臂屈伸)"
        )
        val match = exercisePattern.find(input)
        if (match != null) {
            entities["exercise"] = match.value
        }

        // Concern type
        when {
            input.contains("卡住") || input.contains("平台") || input.contains("不涨") ||
                input.contains("停滞") || input.contains("突破") -> entities["concern"] = "plateau"
            input.contains("怎么练") || input.contains("该练什么") || input.contains("什么部位") ||
                input.contains("明天练") || input.contains("下次练") -> entities["concern"] = "planning"
            input.contains("疼") || input.contains("不舒服") -> entities["concern"] = "discomfort"
            input.contains("问题") || input.contains("改进") || input.contains("不足") ->
                entities["concern"] = "improvement"
            else -> entities["concern"] = "general"
        }

        return entities
    }

    private fun extractReviewEntities(input: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()
        when {
            input.contains("本周") || input.contains("这周") -> entities["period"] = "this_week"
            input.contains("上周") -> entities["period"] = "last_week"
            input.contains("本月") || input.contains("这个月") -> entities["period"] = "this_month"
            input.contains("上月") || input.contains("上个月") -> entities["period"] = "last_month"
            else -> entities["period"] = "this_week"
        }
        return entities
    }

    private fun extractManageEntities(input: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()
        when {
            input.contains("删除") -> entities["action"] = "delete"
            input.contains("存为模板") || input.contains("保存为模板") || input.contains("创建模板") ->
                entities["action"] = "create_template"
            input.contains("模板") && input.contains("叫") ->
                entities["action"] = "create_template"
        }
        return entities
    }

    // ═══════════════════════════════════════════
    // Pattern Matchers
    // ═══════════════════════════════════════════

    private fun String.matchesRecord(): Boolean {
        return contains(Regex(
            "练了|做了|今天|刚[做练]|记录|加.*组|追加|再加|跑了|游了|骑了|打卡|练胸|练背|练腿|练肩|练手臂|练腹"
        )) || (contains(Regex("\\d+.*[kK][gG]|\\d+.*组|\\d+.*次")) && length > 8)
    }

    private fun String.matchesQuery(): Boolean {
        return contains(Regex(
            "几次|多少|什么.*时候|趋势|对比|进步|变化|看一下|查一下|哪天|哪个|几组|多重|容量|频率|分布|怎么样|如何|练得"
        ))
    }

    private fun String.matchesAdvice(): Boolean {
        return contains(Regex(
            "建议|怎么办|卡住|平台期|规划|推荐|帮.*看|怎么.*练|该练什么|明天练|下次练|建议|帮我.*分析|帮我.*看|给点|出出主意"
        ))
    }

    private fun String.matchesReview(): Boolean {
        return contains(Regex("总结|复盘|周报|月报|回顾|汇总|概况|总览")) ||
            (contains(Regex("怎么样$|如何$")) && length < 15)
    }

    private fun String.matchesManage(): Boolean {
        return contains(Regex("删除|删了|存为模板|保存为模板|创建模板"))
    }
}

/**
 * Body part keyword hints for entity extraction during recording.
 */
private object BodyPartHints {
    val entries = listOf(
        Hint("练胸", "胸部"),
        Hint("胸训", "胸部"),
        Hint("练背", "背部"),
        Hint("背训", "背部"),
        Hint("练腿", "腿部"),
        Hint("腿训", "腿部"),
        Hint("练肩", "肩部"),
        Hint("肩训", "肩部"),
        Hint("二头", "二头肌"),
        Hint("三头", "三头肌"),
        Hint("练腹", "核心"),
        Hint("核心", "核心"),
        Hint("全身", "全身"),
    )

    data class Hint(val keyword: String, val bodyPartName: String)
}
