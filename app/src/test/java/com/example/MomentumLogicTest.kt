package com.example

import com.example.core.util.DateUtils
import com.example.data.model.*
import com.example.service.AnalyticsService
import com.example.service.InsightsEngine
import org.junit.Assert.*
import org.junit.Test

class MomentumLogicTest {

    @Test
    fun testPriorityCompletionRate_withData() {
        val tasks = listOf(
            TaskItem(id = 1, title = "Task 1", plannedDate = "2026-10-29", priority = Priority.HIGH, status = TaskStatus.COMPLETED),
            TaskItem(id = 2, title = "Task 2", plannedDate = "2026-10-29", priority = Priority.HIGH, status = TaskStatus.PLANNED),
            TaskItem(id = 3, title = "Task 3", plannedDate = "2026-10-29", priority = Priority.MEDIUM, status = TaskStatus.COMPLETED)
        )

        val metric = AnalyticsService.calculateHighPriorityCompletionRate(tasks)
        assertTrue(metric.hasData)
        assertEquals(50f, metric.value ?: 0f, 0.1f)
        assertEquals("50%", metric.displayText)
    }

    @Test
    fun testPriorityCompletionRate_emptyState_doesNotReturnZeroPercent() {
        val tasks = emptyList<TaskItem>()
        val metric = AnalyticsService.calculateHighPriorityCompletionRate(tasks)
        assertFalse(metric.hasData)
        assertNull(metric.value)
        assertEquals("N/A", metric.displayText)
    }

    @Test
    fun testEstimationError() {
        val tasks = listOf(
            TaskItem(
                id = 1,
                title = "Task 1",
                plannedDate = "2026-10-29",
                estimatedMinutes = 30,
                actualMinutes = 45,
                status = TaskStatus.COMPLETED
            ),
            TaskItem(
                id = 2,
                title = "Task 2",
                plannedDate = "2026-10-29",
                estimatedMinutes = 20,
                actualMinutes = 25,
                status = TaskStatus.COMPLETED
            )
        )

        val metric = AnalyticsService.calculateEstimationError(tasks)
        assertTrue(metric.hasData)
        // Total est: 50, Total act: 70 -> diff = +20
        assertEquals(20f, metric.value ?: 0f, 0.1f)
        assertEquals("+20m", metric.displayText)
    }

    @Test
    fun testRecallRateCalculation() {
        val logs = listOf(
            RevisionLog(id = 1, learningTopicId = 1, dueDate = "2026-10-28", recallRating = RecallRating.RECALLED_SUCCESSFULLY.name),
            RevisionLog(id = 2, learningTopicId = 1, dueDate = "2026-10-29", recallRating = RecallRating.RECALLED_SUCCESSFULLY.name),
            RevisionLog(id = 3, learningTopicId = 2, dueDate = "2026-10-29", recallRating = RecallRating.NEEDED_RELEARNING.name),
            RevisionLog(id = 4, learningTopicId = 2, dueDate = "2026-10-29", recallRating = RecallRating.RECALLED_WITH_HINTS.name)
        )

        val metric = AnalyticsService.calculateRecallRate(logs)
        assertTrue(metric.hasData)
        // 2 of 4 were successfully recalled -> 50%
        assertEquals(50f, metric.value ?: 0f, 0.1f)
        assertEquals("50%", metric.displayText)
    }

    @Test
    fun testDistractionSummaryCalculations() {
        val distractions = listOf(
            Distraction(id = 1, category = "phone_use", durationMinutes = 30),
            Distraction(id = 2, category = "phone_use", durationMinutes = 10),
            Distraction(id = 3, category = "notifications", durationMinutes = 20)
        )

        val summaries = AnalyticsService.calculateDistractionSummaries(distractions)
        assertEquals(2, summaries.size)

        val phone = summaries.first { it.category == "Phone use" }
        assertEquals(40, phone.totalMinutes)
        assertEquals(2, phone.count)
        // 40 / 60 = 66.67%
        assertEquals(66.67f, phone.percentageOfTotal, 0.5f)
    }

    @Test
    fun testInsightsEngine_detectsUnderestimation() {
        val tasks = listOf(
            TaskItem(id = 1, title = "T1", plannedDate = "2026-10-20", estimatedMinutes = 30, actualMinutes = 50, status = TaskStatus.COMPLETED),
            TaskItem(id = 2, title = "T2", plannedDate = "2026-10-21", estimatedMinutes = 30, actualMinutes = 55, status = TaskStatus.COMPLETED),
            TaskItem(id = 3, title = "T3", plannedDate = "2026-10-22", estimatedMinutes = 30, actualMinutes = 45, status = TaskStatus.COMPLETED)
        )

        val insights = InsightsEngine.generateInsights(tasks, emptyList(), emptyList(), emptyList(), emptyList())
        val estimationInsight = insights.firstOrNull { it.type == "ESTIMATION" }
        assertNotNull(estimationInsight)
        assertEquals("Estimation Underestimation", estimationInsight?.title)
        assertFalse(estimationInsight?.isPositive ?: true)
    }

    @Test
    fun testDateUtils_weekCalculations() {
        val today = "2026-10-29"
        val startOfWeek = DateUtils.getStartOfWeek(today)
        val endOfWeek = DateUtils.getEndOfWeek(today)
        assertTrue(startOfWeek <= today)
        assertTrue(endOfWeek >= today)

        val tomorrow = DateUtils.addDays(today, 1)
        assertEquals("2026-10-30", tomorrow)
    }
}
