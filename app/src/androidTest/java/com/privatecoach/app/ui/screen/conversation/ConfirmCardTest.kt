package com.privatecoach.app.ui.screen.conversation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.ParsedExercise
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.ui.screen.conversation.components.bubbles.ConfirmCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConfirmCardTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun appendAndOverwriteAreDistinctActions() {
        var appended = false
        var overwritten = false
        composeRule.setContent {
            ConfirmCard(
                parsedResult = AiParsedResult(
                    WorkoutType.STRENGTH, null,
                    listOf(ParsedExercise("卧推", 60.0, "kg", 4, 8, null, null)),
                    null, null, "完成", "{}"
                ),
                sourceText = "卧推60公斤四组",
                isAppendMode = true,
                onConfirm = {}, onEdit = {}, onCancel = {},
                onAppend = { appended = true }, onOverwrite = { overwritten = true }
            )
        }
        composeRule.onNodeWithText("📎 追加到今天的训练").performClick()
        assertTrue(appended)
        composeRule.onNodeWithText("🔄 覆盖今天的记录").performClick()
        assertTrue(overwritten)
    }
}
