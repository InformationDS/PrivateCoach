package com.privatecoach.app.core.query

import com.privatecoach.app.core.model.IntentType
import com.privatecoach.app.core.model.QueryEngineResult
import com.privatecoach.app.core.model.TrainingFrequencyPoint
import com.privatecoach.app.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class QueryEngineTest {
    private val repository = mockk<WorkoutRepository>()
    private val engine = QueryEngine(repository)

    @Test fun `frequency uses calendar period rather than active days`() = runTest {
        coEvery { repository.getTrainingFrequency(any(), any()) } returns
            listOf(TrainingFrequencyPoint(LocalDate.now(), 1))

        val result = engine.execute(IntentType.QUERY, mapOf("metric" to "frequency", "timeRange" to "this_month"))
            as QueryEngineResult.DataCardNeeded

        assertFalse(result.stats.first().value.startsWith("7.0"))
    }

    @Test fun `last week is the immediately preceding monday to sunday`() = runTest {
        val start = slot<LocalDate>()
        val end = slot<LocalDate>()
        coEvery { repository.getTrainingDaysCount(capture(start), capture(end)) } returns 0

        engine.execute(IntentType.QUERY, mapOf("metric" to "training_days", "timeRange" to "last_week"))

        val thisMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        assertEquals(thisMonday.minusWeeks(1), start.captured)
        assertEquals(thisMonday.minusDays(1), end.captured)
        coVerify(exactly = 1) { repository.getTrainingDaysCount(any(), any()) }
    }
}
