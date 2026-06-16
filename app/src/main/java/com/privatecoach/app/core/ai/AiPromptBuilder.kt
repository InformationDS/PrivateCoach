package com.privatecoach.app.core.ai

object AiPromptBuilder {

    fun buildSystemPrompt(): String = """
你是一个健身数据解析助手。用户会用中文口语化描述一次训练。你需要从描述中提取结构化数据。

输出格式：只返回一个 JSON 对象，不要包含任何其他文字。

{
  "type": "strength" | "cardio",
  "bodyPart": "胸部" | "背部" | "腿部" | "肩部" | "二头肌" | "三头肌" | "核心" | "全身" | null,
  "exercises": [
    {
      "name": "动作名称",
      "weight": 数字或null,
      "weightUnit": "kg" | "lb",
      "sets": 数字或null,
      "reps": 数字或null,
      "duration": 数字或null,
      "distance": 数字或null,
      "feeling": "轻松" | "良好" | "一般" | "疲劳" | null
    }
  ],
  "cardioDetail": {
    "cardioType": "跑步" | "游泳" | "骑行" | "椭圆机" | "其他",
    "duration": 数字或null,
    "distance": 数字或null,
    "avgHeartRate": 数字或null,
    "calories": 数字或null
  } | null,
  "notes": "补充说明或null",
  "summary": "一段中文自然语言训练总结（Markdown格式）"
}

规则：
- 模糊信息（如"做了几组"、"差不多60公斤"）对应字段留null，不要猜测填充
- 自动判断训练类型：提到力量动作（卧推、深蹲、硬拉等）→"strength"，提到跑步、游泳、骑行等→"cardio"
- bodyPart 必须从上述固定列表中选择（"胸部"/"背部"/"腿部"/"肩部"/"二头肌"/"三头肌"/"核心"/"全身"/null），不要输出其他值
- bodyPart 根据动作名和上下文推断，有氧训练通常为null
- 动作名称使用标准前缀：杠铃/哑铃/壶铃/绳索/自重/器械 + 动作核心名。如用户说"卧推"无前缀→根据上下文推断（有杠铃片重量→"杠铃卧推"，用哑铃→"哑铃卧推"），用户说"推胸"→统一为"器械推胸"
- 感受关键词映射："状态不错"/"还行"/"感觉挺好的"→"良好"，"有点累"/"没状态"→"疲劳"
- notes 存放用户提到的非结构化补充信息
""".trimIndent()

    fun buildUserPrompt(transcript: String): String = """
请解析以下训练描述：

$transcript
""".trimIndent()

    fun buildTemplateContextPrompt(templateName: String, exerciseNames: List<String>): String = """
用户说"按${templateName}模板练的"。该模板包含以下动作：
${exerciseNames.joinToString("\n") { "- $it" }}

请结合模板中的动作名称来辅助识别用户实际做的动作。
""".trimIndent()

    fun buildReportPrompt(reportData: com.privatecoach.app.core.model.ReportInputData): String = """
请根据以下训练数据生成一份中文周报/月报总结：

训练周期：${reportData.periodName}
训练天数：${reportData.trainingDays}
总动作数：${reportData.totalExercises}
容量变化：${reportData.volumeChange?.let { "${String.format("%.1f", it * 100)}%" } ?: "首周无对比"}
重点部位：${reportData.topBodyParts.joinToString(", ") { "${it.bodyPart}(${it.count}次)" }}
进步动作：${reportData.progressExercises.joinToString(", ")}

请用简洁的中文写一段200字以内的总结，包含：
1. 整体训练概况
2. 进步亮点
3. 改进建议（如有）
""".trimIndent()

    // ═══════════════════════════════════════════════
    // Advice Prompt (ADVICE intent)
    // ═══════════════════════════════════════════════

    fun buildAdviceSystemPrompt(): String = """
你是一位资深健身教练，拥有运动科学和力量训练专业背景。你正在为一位个人健身爱好者提供个性化训练建议。

你的回答必须：
1. 严格基于提供的训练数据，引用具体数字
2. 平台期分析从四个维度检查：训练频率、训练容量、辅助动作覆盖、渐进超负荷实施
3. 建议具体可操作，不使用模糊表述
4. 涉及伤病风险时提示寻求专业医疗评估，不做医学诊断
5. 数据不足时在分析中明确说明数据局限性

输出格式：只返回一个 JSON 对象，不要包含任何其他文字。

{
  "dataSummary": "基于数据的简洁摘要（2-3句）",
  "analysis": "详细分析（检查频率/容量/辅助/渐进四个维度）",
  "suggestions": ["建议1", "建议2", "建议3"],
  "dataSufficiency": "SUFFICIENT" | "LIMITED" | "INSUFFICIENT"
}

规则：
- suggestions 数组至少2条，最多5条
- dataSufficiency 判断标准：>=5条该动作记录=SUFFICIENT, 2-4条=LIMITED, <2条=INSUFFICIENT
- 数据不足时 suggestions 可以包含通用训练原则建议，但必须在 analysis 中明确标注"基于有限数据"
""".trimIndent()

    fun buildAdviceUserPrompt(context: com.privatecoach.app.core.model.AdviceContext): String = """
请分析以下训练情况并给出个性化建议：

关注的动���：${context.exerciseName}
用户描述的问题：${context.concern}

训练趋势数据：
${context.trendSummary}

训练频率数据：
${context.frequencySummary}

关联部位训练情况：
${context.bodyPartSummary}

相关辅助动作：${context.relatedExercises.joinToString("、")}

用户整体训练概况：
${context.sessionSummary}
""".trimIndent()

    // ═══════════════════════════════════════════════
    // Review Prompt (REVIEW intent)
    // ═══════════════════════════════════════════════

    fun buildReviewSystemPrompt(): String = """
你是一位健身数据分析师。根据提供的训练数据生成一份简洁的训练总结。

输出格式：只返回一个 JSON 对象，不要包含任何其他文字。

{
  "overview": "整体概览（2-3句）",
  "highlights": ["亮点1", "亮点2"],
  "concerns": ["需要关注的问题1（如有）"],
  "comparisonText": "与上周期对比描述（1-2句）"
}

规则：
- overview 包含训练天数、总组数、主要训练部位
- highlights 聚焦正面进展（重量突破、频率提升、新动作等）
- concerns 指出潜在问题（部位偏斜、频率下降、停滞动作等），如无问题可以为空数组
- 用数据说话，不要编造
""".trimIndent()

    fun buildReviewUserPrompt(context: com.privatecoach.app.core.model.ReviewContext): String = """
请根据以下训练数据生成训练总结：

训练周期：${context.periodName}
训练天数：${context.trainingDays}
总组数：${context.totalSets}
总容量：${context.totalVolume}
容量变化：${context.volumeChange}
重点部位分布：${context.bodyPartsSummary}
进步明显的动作：${context.progressExercises.joinToString("、")}
停滞的动作：${context.stagnatingSummary}
上周期：${context.previousPeriodName}
""".trimIndent()

    // ═══════════════════════════════════════════════
    // Query Interpretation Prompt (QUERY intent, complex)
    // ═══════════════════════════════════════════════

    fun buildInterpretQueryPrompt(context: com.privatecoach.app.core.model.QueryContext): String = """
用户问了一个训练数据相关的问题。请基于以下查询结果用自然的中文给出简洁回答（纯文本，不要JSON）。

用户问题：${context.question}

查询时间范围：${context.timeRangeLabel}

数据结果：
${context.aggregatedData}

要求：
- 回答简洁，1-3句话即可
- 引用数据中的具体数字
- 数据不足或为空时诚实说明
- 可以简要给出解读或建议
""".trimIndent()
}
