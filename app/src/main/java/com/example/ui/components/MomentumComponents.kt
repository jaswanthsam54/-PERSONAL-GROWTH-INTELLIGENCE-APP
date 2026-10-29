package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.DateUtils
import com.example.data.model.Priority
import com.example.data.model.TaskStatus
import com.example.service.DayFocusSummary
import com.example.service.HeatmapDay
import com.example.service.TimerState
import com.example.ui.theme.*

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (priority) {
        Priority.HIGH -> Pair(Color(0xFFFFE4E6), MomentumRose)
        Priority.MEDIUM -> Pair(Color(0xFFFEF3C7), MomentumAmber)
        Priority.LOW -> Pair(Color(0xFFE0E7FF), MomentumIndigo)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("priority_badge_${priority.name.lowercase()}")
    ) {
        Text(
            text = priority.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: TaskStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        TaskStatus.COMPLETED -> Pair(Color(0xFFD1FAE5), MomentumGreen)
        TaskStatus.IN_PROGRESS -> Pair(Color(0xFFDBEAFE), MomentumIndigoLight)
        TaskStatus.PARTIALLY_COMPLETED -> Pair(Color(0xFFFEF3C7), MomentumAmber)
        TaskStatus.SKIPPED -> Pair(Color(0xFFF1F5F9), Slate600)
        TaskStatus.PLANNED -> Pair(Color(0xFFF1F5F9), Slate800)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("status_badge_${status.name.lowercase()}")
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    supportingText: String = "",
    icon: ImageVector? = null,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stat_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = valueColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            if (supportingText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("empty_state_action_button")
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun WeeklyFocusBarChart(
    days: List<DayFocusSummary>,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) {
        Text("No focus sessions logged this week.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val maxMinutes = days.maxOfOrNull { it.focusedMinutes }?.coerceAtLeast(60) ?: 60

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                val fraction = (day.focusedMinutes.toFloat() / maxMinutes).coerceIn(0.04f, 1f)
                val isToday = DateUtils.isDateToday(day.date)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (day.focusedMinutes > 0) {
                        Text(
                            text = "${day.focusedMinutes}m",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primary
                                else if (day.focusedMinutes > 0) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                val isToday = DateUtils.isDateToday(day.date)
                Text(
                    text = day.dayOfWeek.take(2),
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActivityHeatmapView(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Activity Heatmap (Past 4 Weeks)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 4 weeks x 7 days grid
        val weeks = days.chunked(7)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeks.forEach { week ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    week.forEach { day ->
                        val cellColor = when (day.level) {
                            0 -> MaterialTheme.colorScheme.surfaceVariant
                            1 -> MomentumGreen.copy(alpha = 0.35f)
                            2 -> MomentumGreen.copy(alpha = 0.60f)
                            3 -> MomentumGreen.copy(alpha = 0.85f)
                            else -> MomentumGreen
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(cellColor)
                                .border(
                                    width = if (DateUtils.isDateToday(day.date)) 1.5.dp else 0.dp,
                                    color = if (DateUtils.isDateToday(day.date)) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            listOf(0, 1, 2, 3, 4).forEach { lvl ->
                val col = when (lvl) {
                    0 -> MaterialTheme.colorScheme.surfaceVariant
                    1 -> MomentumGreen.copy(alpha = 0.35f)
                    2 -> MomentumGreen.copy(alpha = 0.60f)
                    3 -> MomentumGreen.copy(alpha = 0.85f)
                    else -> MomentumGreen
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(col)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ActiveTimerFloatingBanner(
    timerState: TimerState,
    onExpand: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = timerState.isRunning || timerState.isPaused) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onExpand() }
                .testTag("active_timer_banner"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Default.Timer else Icons.Default.PauseCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = timerState.activeTaskTitle ?: "Deep Focus Session",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (timerState.isPaused) "Paused • Tap to resume" else "In Progress • Active Focus",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateUtils.formatSeconds(timerState.elapsedSeconds),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { if (timerState.isRunning) onPause() else onResume() },
                        modifier = Modifier.testTag("banner_timer_toggle")
                    ) {
                        Icon(
                            imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (timerState.isRunning) "Pause" else "Resume",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
