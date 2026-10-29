package com.example.service

import com.example.core.util.DateUtils
import com.example.data.model.*

data class PersonalInsight(
    val title: String,
    val observation: String,
    val periodAnalyzed: String,
    val supportingNumbers: String,
    val suggestedAction: String,
    val isPositive: Boolean = false,
    val type: String // ESTIMATION, DISTRACTION, FOCUS, REVISION, PRIORITY
)

object InsightsEngine {

    fun generateInsights(
        tasks: List<TaskItem>,
        sessions: List<FocusSession>,
        distractions: List<Distraction>,
        revisionLogs: List<RevisionLog>,
        goals: List<Goal>
    ): List<PersonalInsight> {
        val insights = mutableListOf<PersonalInsight>()

        // 1. Time Estimation Insight
        val completedWithTime = tasks.filter {
            it.status == TaskStatus.COMPLETED && it.estimatedMinutes > 0 && it.actualMinutes > 0
        }
        if (completedWithTime.size >= 3) {
            val totalEst = completedWithTime.sumOf { it.estimatedMinutes }
            val totalAct = completedWithTime.sumOf { it.actualMinutes }
            val diff = totalAct - totalEst
            val percentOff = ((diff.toFloat() / totalEst) * 100).toInt()

            if (diff > 15 && percentOff >= 20) {
                insights.add(
                    PersonalInsight(
                        title = "Estimation Underestimation",
                        observation = "Your completed tasks took noticeably longer than planned.",
                        periodAnalyzed = "Recent ${completedWithTime.size} tasks",
                        supportingNumbers = "Planned: ${totalEst}m | Actual: ${totalAct}m (+${diff}m, +${percentOff}%)",
                        suggestedAction = "Try adding a 25% buffer to your task estimates or breaking larger tasks into 20-minute chunks.",
                        isPositive = false,
                        type = "ESTIMATION"
                    )
                )
            } else if (diff in -10..10) {
                insights.add(
                    PersonalInsight(
                        title = "Precise Time Calibrations",
                        observation = "Your time estimates closely match your actual execution duration.",
                        periodAnalyzed = "Recent ${completedWithTime.size} tasks",
                        supportingNumbers = "Planned: ${totalEst}m | Actual: ${totalAct}m (variance within ±${Math.abs(diff)}m)",
                        suggestedAction = "Keep this estimation cadence when scheduling your daily peak work hours.",
                        isPositive = true,
                        type = "ESTIMATION"
                    )
                )
            }
        }

        // 2. Distraction Driver Insight
        if (distractions.isNotEmpty()) {
            val totalDistractionMin = distractions.sumOf { it.durationMinutes }.coerceAtLeast(1)
            val byCategory = distractions.groupBy { it.category }
            val topDistraction = byCategory.maxByOrNull { entry -> entry.value.sumOf { it.durationMinutes } }

            if (topDistraction != null) {
                val topMin = topDistraction.value.sumOf { it.durationMinutes }
                val share = ((topMin.toFloat() / totalDistractionMin) * 100).toInt()
                val catName = topDistraction.key.replace('_', ' ').replaceFirstChar { it.uppercase() }

                if (share >= 35 && totalDistractionMin >= 10) {
                    insights.add(
                        PersonalInsight(
                            title = "Primary Distraction Source: $catName",
                            observation = "$catName accounted for $share% of all your recorded distraction time.",
                            periodAnalyzed = "All recorded distraction events",
                            supportingNumbers = "$topMin minutes out of $totalDistractionMin total lost minutes (${topDistraction.value.size} occurrences)",
                            suggestedAction = "Before your next focus block, place your phone out of arm's reach or silence non-critical notifications.",
                            isPositive = false,
                            type = "DISTRACTION"
                        )
                    )
                }
            }
        }

        // 3. Weekly Focus Momentum Insight
        val weekly = AnalyticsService.calculateWeeklyComparison(tasks, sessions)
        if (weekly.currentFocusMinutes > 0 || weekly.previousFocusMinutes > 0) {
            val delta = weekly.focusDeltaMinutes
            if (delta > 30) {
                insights.add(
                    PersonalInsight(
                        title = "Focus Time Surge",
                        observation = "Your recorded focus time expanded compared to last week.",
                        periodAnalyzed = "This week vs. Last week",
                        supportingNumbers = "This week: ${weekly.currentFocusMinutes}m | Last week: ${weekly.previousFocusMinutes}m (+${delta}m)",
                        suggestedAction = "Capitalize on this positive momentum by protecting your morning focus windows.",
                        isPositive = true,
                        type = "FOCUS"
                    )
                )
            } else if (delta < -30 && weekly.previousFocusMinutes > 0) {
                insights.add(
                    PersonalInsight(
                        title = "Focus Dip Observed",
                        observation = "You have logged fewer focus minutes this week than the prior week.",
                        periodAnalyzed = "This week vs. Last week",
                        supportingNumbers = "This week: ${weekly.currentFocusMinutes}m | Last week: ${weekly.previousFocusMinutes}m (${delta}m)",
                        suggestedAction = "Schedule one guaranteed 25-minute uninterrupted block today to regain rhythm.",
                        isPositive = false,
                        type = "FOCUS"
                    )
                )
            }
        }

        // 4. Revision Recall Retention Insight
        if (revisionLogs.size >= 2) {
            val successful = revisionLogs.count { it.recallRating == RecallRating.RECALLED_SUCCESSFULLY.name }
            val rate = ((successful.toFloat() / revisionLogs.size) * 100).toInt()

            if (rate < 60) {
                insights.add(
                    PersonalInsight(
                        title = "Knowledge Retention Gap",
                        observation = "Recall accuracy on spaced revisions is currently below 60%.",
                        periodAnalyzed = "Across ${revisionLogs.size} completed reviews",
                        supportingNumbers = "$successful of ${revisionLogs.size} reviews recalled cleanly ($rate% recall rate)",
                        suggestedAction = "Use active retrieval (write summary from memory before reviewing notes) rather than passive reading.",
                        isPositive = false,
                        type = "REVISION"
                    )
                )
            } else if (rate >= 80) {
                insights.add(
                    PersonalInsight(
                        title = "Strong Memory Retention",
                        observation = "Your spaced repetition habits are yielding high recall consistency.",
                        periodAnalyzed = "Across ${revisionLogs.size} completed reviews",
                        supportingNumbers = "$successful of ${revisionLogs.size} reviews recalled without hints ($rate% recall rate)",
                        suggestedAction = "Consider expanding revision intervals to 14 days for these mastered topics.",
                        isPositive = true,
                        type = "REVISION"
                    )
                )
            }
        }

        // 5. High Priority Completion Insight
        val highPriorities = tasks.filter { it.priority == Priority.HIGH }
        if (highPriorities.size >= 3) {
            val completedHigh = highPriorities.count { it.status == TaskStatus.COMPLETED }
            val rate = ((completedHigh.toFloat() / highPriorities.size) * 100).toInt()

            if (rate >= 75) {
                insights.add(
                    PersonalInsight(
                        title = "High Priority Execution",
                        observation = "You are consistently converting your most critical priorities into finished work.",
                        periodAnalyzed = "All ${highPriorities.size} recorded High-Priority tasks",
                        supportingNumbers = "$completedHigh of ${highPriorities.size} high priorities completed ($rate%)",
                        suggestedAction = "Continue limiting high priority designations to 1-3 essentials per day.",
                        isPositive = true,
                        type = "PRIORITY"
                    )
                )
            }
        }

        return insights
    }
}
