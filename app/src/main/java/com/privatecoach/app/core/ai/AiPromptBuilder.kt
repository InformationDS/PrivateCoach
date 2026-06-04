package com.privatecoach.app.core.ai

object AiPromptBuilder {

    fun buildSystemPrompt(): String = """
你是一个健身数据解析助手。用户会用中文口语化描述一次训练。你需要从描述中提取结构化数据。

输出格式：只返回一个 JSON 对象，不要包含任何其他文字。

{
  "type": "strength" | "cardio",
  "bodyPart": "胸部" | "背部" | "腿部" | "肩部" | "手臂" | "核心" | "全身" | null,
  "exercises": [
    {
      "name": "动作名称",
      "weight": 数字或null,
      "weightUnit": "kg" | "lb",
      "sets": 数字或null,
      "reps": 数字或null,
      "duration": 数字或null,
      "distance": 数字或null
    }
  ],
  "cardioDetail": {
    "cardioType": "跑步" | "游泳" | "骑行" | "椭圆机" | "其他",
    "duration": 数字或null,
    "distance": 数字或null,
    "avgHeartRate": 数字或null,
    "calories": 数字或null
  } | null,
  "feeling": "轻松" | "良好" | "一般" | "疲劳" | null,
  "notes": "补充说明或null",
  "summary": "一段中文自然语言训练总结（Markdown格式）"
}

规则：
- 模糊信息（如"做了几组"、"差不多60公斤"）对应字段留null，不要猜测填充
- 自动判断训练类型：提到力量动作（卧推、深蹲、硬拉等）→"strength"，提到跑步、游泳、骑行等→"cardio"
- bodyPart 根据动作名和上下文推断，有氧训练通常为null
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
}
