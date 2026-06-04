package com.privatecoach.app.core.model

enum class WorkoutType { STRENGTH, CARDIO }
enum class InputMode { VOICE, TEXT, MANUAL }
enum class Feeling { EASY, GOOD, NORMAL, TIRED }

fun Feeling.toChinese(): String = when (this) {
    Feeling.EASY -> "轻松"
    Feeling.GOOD -> "良好"
    Feeling.NORMAL -> "一般"
    Feeling.TIRED -> "疲劳"
}

fun String.toFeeling(): Feeling? = when (this) {
    "轻松" -> Feeling.EASY
    "良好" -> Feeling.GOOD
    "一般" -> Feeling.NORMAL
    "疲劳" -> Feeling.TIRED
    else -> null
}
