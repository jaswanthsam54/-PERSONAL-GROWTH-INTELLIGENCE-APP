package com.example.service

import com.example.core.util.DateUtils
import com.example.data.model.*

data class MetricValue(
    val value: Float?, // null if insufficient data / zero denominator
    val displayText: String,
    val supportingText: String = "",
    val hasData: Boolean = value != null
)

data class DayFocusSummary(
    val date: String,
    val dayOfWeek: String,
    val focusedMinutes: Int
)

data class PriorityCompletionSummary(
    val priority: Priority,
    val total: Int,
    val completed: Int,
    val rate: Float? // null if total is 0
)

data class CategoryTimeError(
    val category: String,
    val totalEstimated: Int,
    val totalActual: Int,
    val errorMinutes: Int, // actual - estimated
    val count: Int
)

data class DistractionSummary(
    val category: String,
    val count: Int,
    val totalMinutes: Int,
    val percentageOfTotal: Float
)

data class HeatmapDay(
    val date: String,
    val activityCount: Int,
    val level: Int // 0..4
)

data class WeeklyComparison(
    val currentWeekStart: String,
    val previousWeekStart: String,
    val currentFocusMinutes: Int,
    val previousFocusMinutes: Int,
    val focusDeltaMinutes: Int,
    val currentCompletedPriorities: Int,
    val currentTotalPriorities: Int,
    val previousCompletedPriorities: Int,
    val previousTotalPriorities: Int,
    val currentPriorityRate: Float?,
    val previousPriorityRate: Float?
)

object AnalyticsService {

    /**
     * Calculates Priority Completion Rate for high priority tasks
     * Completed high priorities / Total high priorities * 100
     */
    fun calculateHighPriorityCompletionRate(tasks: List<TaskItem>): MetricValue {
        val highPriorities = tasks.filter { it.priority == Priority.HIGH }
        if (highPriorities.isEmpty()) {
            return MetricValue(
                value = null,
                displayText = "N/A",
                supportingText = "No high priority tasks recorded"
            )
        }
        val completed = highPriorities.count { it.status == TaskStatus.COMPLETED }
        val rate = (completed.toFloat() / highPriorities.size) * 100f
        return MetricValue(
            value = rate,
            displayText = "%.0f%%".format(rate),
            supportingText = "$completed of ${highPriorities.size} completed"
        )
    }

    /**
     * Overall Priority Completion Rate:
     * Completed planned tasks / Total planned tasks * 100
     */
    fun calculateOverallCompletionRate(tasks: List<TaskItem>): MetricValue {
        val plannedTasks = tasks.filter { it.status != TaskStatus.SKIPPED }
        if (plannedTasks.isEmpty()) {
            return MetricValue(
                value = null,
                displayText = "N/A",
                supportingText = "No planned tasks"
            )
        }
        val completed = plannedTasks.count { it.status == TaskStatus.COMPLETED }
        val rate = (completed.toFloat() / plannedTasks.size) * 100f
        return MetricValue(
            value = rate,
            displayText = "%.0f%%".format(rate),
            supportingText = "$completed of ${plannedTasks.size} tasks finished"
        )
    }

    /**
     * Total focused minutes from completed/valid sessions.
     * Prevents counting cancelled sessions with 0 seconds.
     */
    fun calculateTotalFocusMinutes(sessions: List<FocusSession>): Int {
        val validSeconds = sessions
            .filter { it.status == SessionStatus.COMPLETED.name || it.focusedSeconds > 0 }
            .sumOf { it.focusedSeconds }
        return validSeconds / 60
    }

    /**
     * Group focused minutes by date for the last 7 days.
     */
    fun getDailyFocusLast7Days(sessions: List<FocusSession>): List<DayFocusSummary> {
        val pastDays = DateUtils.getPastDays(7)
        val sessionsByDate = mutableMapOf<String, Int>()

        for (session in sessions) {
            if (session.focusedSeconds > 0) {
                val date = DateUtils.dateFromMillis(session.startedAt)
                sessionsByDate[date] = (sessionsByDate[date] ?: 0) + session.focusedSeconds
            }
        }

        return pastDays.map { date ->
            val seconds = sessionsByDate[date] ?: 0
            DayFocusSummary(
                date = date,
                dayOfWeek = DateUtils.getDayOfWeek(date),
                focusedMinutes = seconds / 60
            )
        }
    }

    /**
     * Time Estimation Error: Actual - Estimated
     * Only calculates for completed or partially completed tasks with recorded actual minutes.
     */
    fun calculateEstimationError(tasks: List<TaskItem>): MetricValue {
        val evaluatedTasks = tasks.filter {
            (it.status == TaskStatus.COMPLETED || it.status == TaskStatus.PARTIALLY_COMPLETED) &&
                    it.estimatedMinutes > 0 && it.actualMinutes > 0
        }

        if (evaluatedTasks.isEmpty()) {
            return MetricValue(
                value = null,
                displayText = "N/A",
                supportingText = "No completed tasks with recorded duration"
            )
        }

        val totalEst = evaluatedTasks.sumOf { it.estimatedMinutes }
        val totalAct = evaluatedTasks.sumOf { it.actualMinutes }
        val diff = totalAct - totalEst
        val sign = if (diff > 0) "+" else ""

        return MetricValue(
            value = diff.toFloat(),
            displayText = "$sign${diff}m",
            supportingText = if (diff > 0) "Exceeding estimates by avg ${(diff / evaluatedTasks.size)}m/task"
            else if (diff < 0) "Finishing under estimate by ${-diff}m"
            else "Estimates precisely on target"
        )
    }

    /**
     * Distraction Summary by Category
     */
    fun calculateDistractionSummaries(distractions: List<Distraction>): List<DistractionSummary> {
        if (distractions.isEmpty()) return emptyList()

        val totalMin = distractions.sumOf { it.durationMinutes }.coerceAtLeast(1)
        val grouped = distractions.groupBy { it.category }

        return grouped.map { (cat, list) ->
            val catMinutes = list.sumOf { it.durationMinutes }
            DistractionSummary(
                category = cat.replace('_', ' ').replaceFirstChar { it.uppercase() },
                count = list.size,
                totalMinutes = catMinutes,
                percentageOfTotal = (catMinutes.toFloat() / totalMin) * 100f
            )
        }.sortedByDescending { it.totalMinutes }
    }

    /**
     * Revision Recall Rate
     * Successful recall attempts / All completed recall attempts * 100
     */
    fun calculateRecallRate(logs: List<RevisionLog>): MetricValue {
        if (logs.isEmpty()) {
            return MetricValue(
                value = null,
                displayText = "N/A",
                supportingText = "No revision attempts yet"
            )
        }
        val successful = logs.count { it.recallRating == RecallRating.RECALLED_SUCCESSFULLY.name }
        val rate = (successful.toFloat() / logs.size) * 100f
        return MetricValue(
            value = rate,
            displayText = "%.0f%%".format(rate),
            supportingText = "$successful of ${logs.size} recalled cleanly"
        )
    }

    /**
     * Weekly comparison: This week vs Previous week
     */
    fun calculateWeeklyComparison(
        tasks: List<TaskItem>,
        sessions: List<FocusSession>
    ): WeeklyComparison {
        val today = DateUtils.todayString()
        val currentWeekStart = DateUtils.getStartOfWeek(today)
        val prevWeekStart = DateUtils.addDays(currentWeekStart, -7)
        val prevWeekEnd = DateUtils.addDays(currentWeekStart, -1)

        val currentStartMs = DateUtils.getDayStartMillis(currentWeekStart)
        val prevStartMs = DateUtils.getDayStartMillis(prevWeekStart)
        val prevEndMs = DateUtils.getDayEndMillis(prevWeekEnd)

        // Focus minutes
        val curFocusMin = sessions
            .filter { it.startedAt >= currentStartMs && it.focusedSeconds > 0 }
            .sumOf { it.focusedSeconds } / 60

        val prevFocusMin = sessions
            .filter { it.startedAt in prevStartMs..prevEndMs && it.focusedSeconds > 0 }
            .sumOf { it.focusedSeconds } / 60

        // Priorities
        val curTasks = tasks.filter { it.plannedDate >= currentWeekStart }
        val curHigh = curTasks.filter { it.priority == Priority.HIGH }
        val curCompletedHigh = curHigh.count { it.status == TaskStatus.COMPLETED }
        val curRate = if (curHigh.isNotEmpty()) (curCompletedHigh.toFloat() / curHigh.size) * 100f else null

        val prevTasks = tasks.filter { it.plannedDate in prevWeekStart..prevWeekEnd }
        val prevHigh = prevTasks.filter { it.priority == Priority.HIGH }
        val prevCompletedHigh = prevHigh.count { it.status == TaskStatus.COMPLETED }
        val prevRate = if (prevHigh.isNotEmpty()) (prevCompletedHigh.toFloat() / prevHigh.size) * 100f else null

        return WeeklyComparison(
            currentWeekStart = currentWeekStart,
            previousWeekStart = prevWeekStart,
            currentFocusMinutes = curFocusMin,
            previousFocusMinutes = prevFocusMin,
            focusDeltaMinutes = curFocusMin - prevFocusMin,
            currentCompletedPriorities = curCompletedHigh,
            currentTotalPriorities = curHigh.size,
            previousCompletedPriorities = prevCompletedHigh,
            previousTotalPriorities = prevHigh.size,
            currentPriorityRate = curRate,
            previousPriorityRate = prevRate
        )
    }

    /**
     * Daily Activity Heatmap (past 28 days)
     */
    fun calculateActivityHeatmap(
        tasks: List<TaskItem>,
        sessions: List<FocusSession>
    ): List<HeatmapDay> {
        val past28Days = DateUtils.getPastDays(28)
        val taskCounts = tasks
            .filter { it.status == TaskStatus.COMPLETED }
            .groupBy { it.plannedDate }
            .mapValues { it.value.size }

        val sessionCounts = sessions
            .filter { it.focusedSeconds >= 300 } // at least 5 mins
            .groupBy { DateUtils.dateFromMillis(it.startedAt) }
            .mapValues { it.value.size }

        return past28Days.map { date ->
            val count = (taskCounts[date] ?: 0) + (sessionCounts[date] ?: 0)
            val level = when {
                count == 0 -> 0
                count <= 2 -> 1
                count <= 4 -> 2
                count <= 6 -> 3
                else -> 4
            }
            HeatmapDay(date = date, activityCount = count, level = level)
        }
    }
}
