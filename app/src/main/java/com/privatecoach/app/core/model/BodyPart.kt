package com.privatecoach.app.core.model

enum class BodyPart(val chineseName: String) {
    CHEST("胸部"),
    BACK("背部"),
    LEGS("腿部"),
    SHOULDERS("肩部"),
    BICEPS("二头肌"),
    TRICEPS("三头肌"),
    CORE("核心"),
    FULL_BODY("全身");

    companion object {
        fun fromChinese(name: String): BodyPart? =
            entries.firstOrNull { it.chineseName == name }

        fun fromString(value: String): BodyPart? =
            runCatching { valueOf(value) }.getOrNull()

        val selectableList: List<BodyPart> = entries.toList()
    }
}
