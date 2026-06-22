package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiResponseParserTest {
    private val parser = AiResponseParser()

    @Test fun `parses valid strength workout`() {
        val result = parser.parse(
            """{"type":"STRENGTH","bodyPart":"胸部","exercises":[{"name":"卧推","weight":60,"sets":4,"reps":8}],"summary":"完成"}"""
        ).getOrThrow()
        assertEquals(WorkoutType.STRENGTH, result.type)
        assertEquals("卧推", result.exercises.single().name)
    }

    @Test fun `rejects empty exercise list`() {
        assertTrue(parser.parse("""{"type":"STRENGTH","exercises":[],"summary":""}""").isFailure)
    }

    @Test fun `rejects negative values`() {
        assertTrue(parser.parse(
            """{"type":"STRENGTH","exercises":[{"name":"卧推","weight":-1}],"summary":""}"""
        ).isFailure)
    }

    @Test fun `creates cardio exercise from cardio details`() {
        val result = parser.parse(
            """{"type":"CARDIO","exercises":[],"cardioDetail":{"cardioType":"跑步","duration":1800},"summary":""}"""
        ).getOrThrow()
        assertEquals("跑步", result.exercises.single().name)
    }
}
