package com.privatecoach.app.core.ai

import com.privatecoach.app.core.model.IntentType
import com.privatecoach.app.core.model.SafetyVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class IntentRouterTest {
    private val router = IntentRouter()

    @Test fun `destructive requests are forbidden`() {
        assertEquals(SafetyVerdict.FORBIDDEN, router.checkSafety("帮我清空全部训练数据"))
    }

    @Test fun `injury requests are high risk`() {
        assertEquals(SafetyVerdict.HIGH_RISK, router.checkSafety("膝盖疼还能继续深蹲吗"))
    }

    @Test fun `local query is classified before record`() {
        assertEquals(IntentType.QUERY, router.classify("这周练了几次").type)
    }

    @Test fun `unknown input requires clarification`() {
        assertEquals(IntentType.AMBIGUOUS, router.classify("随便聊聊人生吧").type)
    }
}
