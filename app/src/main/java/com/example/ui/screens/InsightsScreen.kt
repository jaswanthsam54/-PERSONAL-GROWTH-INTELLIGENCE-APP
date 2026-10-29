package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.util.DateUtils
import com.example.service.AnalyticsService
import com.example.service.PersonalInsight
import com.example.ui.MomentumViewModel
import com.example.ui.components.ActivityHeatmapView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.WeeklyFocusBarChart
import com.example.ui.theme.*

@Composable
fun InsightsScreen(
    viewModel: MomentumViewModel
) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val allDistractions by viewModel.allDistractions.collectAsStateWithLifecycle()
    val allRevisionLogs by viewModel.allRevisionLogs.collectAsStateWithLifecycle()
    val insights by viewModel.derivedInsights.collectAsStateWithLifecycle()
    val weeklyComparison by viewModel.weeklyComparison.collectAsStateWithLifecycle()
    val heatmapDays by viewModel.heatmapDays.collectAsStateWithLifecycle()
    val distractionSummaries by viewModel.distractionSummaries.collectAsStateWithLifecycle()
    val dailyFocusLast7Days by viewModel.dailyFocusLast7Days.collectAsStateWithLifecycle()

    val highPriorityMetric = AnalyticsService.calculateHighPriorityCompletionRate(allTasks)
    val overallCompletionMetric = AnalyticsService.calculateOverallCompletionRate(allTasks)
    val timeEstimationMetric = AnalyticsService.calculateEstimationError(allTasks)
    val recallMetric = AnalyticsService.calculateRecallRate(allRevisionLogs)
    val totalFocusMinutes = AnalyticsService.calculateTotalFocusMinutes(allSessions)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("insights_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // HEADER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        text = "GROWTH INTELLIGENCE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Analytics & Insights",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Empirical personal productivity derived strictly from your genuine records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // PRIMARY 4-METRIC GRID
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Priority Completion",
                        value = highPriorityMetric.displayText,
                        supportingText = highPriorityMetric.supportingText,
                        icon = Icons.Default.Verified,
                        valueColor = if (highPriorityMetric.hasData) MomentumGreen else Slate600,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Focused Time",
                        value = "${totalFocusMinutes}m",
                        supportingText = "${allSessions.count { it.focusedSeconds > 0 }} total sessions",
                        icon = Icons.Default.Timer,
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Estimation Error",
                        value = timeEstimationMetric.displayText,
                        supportingText = timeEstimationMetric.supportingText,
                        icon = Icons.Default.CompareArrows,
                        valueColor = if (timeEstimationMetric.hasData) {
                            if ((timeEstimationMetric.value ?: 0f) > 10f) MomentumAmber else MomentumGreen
                        } else Slate600,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Revision Recall",
                        value = recallMetric.displayText,
                        supportingText = recallMetric.supportingText,
                        icon = Icons.Default.Psychology,
                        valueColor = if (recallMetric.hasData) MomentumIndigoLight else Slate600,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // WEEK-OVER-WEEK COMPARISON CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Week-over-Week Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Focus Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${weeklyComparison.currentFocusMinutes}m vs ${weeklyComparison.previousFocusMinutes}m",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val focusDelta = weeklyComparison.focusDeltaMinutes
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (focusDelta >= 0) Color(0xFFD1FAE5) else Color(0xFFFFE4E6))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = (if (focusDelta >= 0) "+${focusDelta}m" else "${focusDelta}m") + " delta",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (focusDelta >= 0) MomentumGreen else MomentumRose
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("High Priority Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val curRateStr = weeklyComparison.currentPriorityRate?.let { "%.0f%%".format(it) } ?: "N/A"
                            val prevRateStr = weeklyComparison.previousPriorityRate?.let { "%.0f%%".format(it) } ?: "N/A"
                            Text(
                                text = "$curRateStr vs $prevRateStr",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${weeklyComparison.currentCompletedPriorities}/${weeklyComparison.currentTotalPriorities} completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // DAILY ACTIVITY HEATMAP
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(modifier = Modifier.padding(18.dp)) {
                    ActivityHeatmapView(days = heatmapDays)
                }
            }
        }

        // DISTRACTION BREAKDOWN
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Distraction Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (distractionSummaries.isEmpty()) {
                        Text(
                            text = "No distractions recorded. Track interruptions during focus blocks to identify top friction triggers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        distractionSummaries.forEach { summary ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = summary.category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${summary.totalMinutes}m (${summary.count}x • ${summary.percentageOfTotal.toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { summary.percentageOfTotal / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MomentumAmber,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // PERSONAL INSIGHTS ENGINE SECTION
        item {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Personal Insights Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Deterministic, rule-based observations grounded in your personal data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (insights.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    EmptyStateView(
                        icon = Icons.Default.Insights,
                        title = "Generating Growth Insights",
                        message = "Insights require at least 3 completed tasks with durations, focus sessions, or revision attempts to reveal reliable patterns."
                    )
                }
            }
        } else {
            items(insights) { insight ->
                PersonalInsightCard(
                    insight = insight,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun PersonalInsightCard(
    insight: PersonalInsight,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("insight_card_${insight.type.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (insight.isPositive) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (insight.isPositive) MomentumGreen.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (insight.isPositive) Icons.Default.CheckCircle else Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = if (insight.isPositive) MomentumGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = insight.periodAnalyzed,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight.observation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Evidence: ${insight.supportingNumbers}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = insight.suggestedAction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
