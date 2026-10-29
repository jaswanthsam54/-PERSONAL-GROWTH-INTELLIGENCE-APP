package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.LearningTopic
import com.example.data.model.RecallRating
import com.example.service.AnalyticsService
import com.example.ui.MomentumViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionScreen(
    viewModel: MomentumViewModel,
    onBack: () -> Unit
) {
    val dueRevisions by viewModel.dueRevisions.collectAsStateWithLifecycle()
    val upcomingRevisions by viewModel.upcomingRevisions.collectAsStateWithLifecycle()
    val allLogs by viewModel.allRevisionLogs.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    var showAddTopicDialog by remember { mutableStateOf(false) }
    var topicToReview by remember { mutableStateOf<LearningTopic?>(null) }

    val recallMetric = AnalyticsService.calculateRecallRate(allLogs)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spaced Revision Tracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("revision_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTopicDialog = true }, modifier = Modifier.testTag("revision_add_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Topic")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTopicDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("revision_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Topic")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("revision_screen"),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp)
        ) {
            // METRICS STATS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Due For Review",
                        value = "${dueRevisions.size}",
                        supportingText = if (dueRevisions.isNotEmpty()) "Pending test" else "All caught up",
                        icon = Icons.Default.Alarm,
                        valueColor = if (dueRevisions.isNotEmpty()) MomentumAmber else MomentumGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Recall Accuracy",
                        value = recallMetric.displayText,
                        supportingText = recallMetric.supportingText,
                        icon = Icons.Default.Psychology,
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // DUE / OVERDUE REVISIONS SECTION
            item {
                Text(
                    text = "Due Revisions (${dueRevisions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Spaced intervals: 1 day → 4 days → 7 days → 14 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (dueRevisions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.CheckCircleOutline,
                            title = "No Pending Revisions",
                            message = "You have completed all scheduled recall reviews for today."
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            } else {
                items(dueRevisions, key = { it.id }) { topic ->
                    RevisionTopicCard(
                        topic = topic,
                        isDue = true,
                        onReview = { topicToReview = topic },
                        onDelete = { viewModel.deleteTopic(topic) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // UPCOMING REVISIONS SECTION
            item {
                Text(
                    text = "Upcoming Revisions (${upcomingRevisions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (upcomingRevisions.isEmpty()) {
                item {
                    Text(
                        text = "No upcoming topics scheduled. Add learning topics to track long-term retention.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(upcomingRevisions, key = { it.id }) { topic ->
                    RevisionTopicCard(
                        topic = topic,
                        isDue = false,
                        onReview = { topicToReview = topic },
                        onDelete = { viewModel.deleteTopic(topic) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    // CONDUCT REVISION REVIEW DIALOG
    if (topicToReview != null) {
        val topic = topicToReview!!
        var selectedRating by remember { mutableStateOf(RecallRating.RECALLED_SUCCESSFULLY) }
        var reviewNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { topicToReview = null },
            title = { Text("Active Recall: ${topic.title}") },
            text = {
                Column {
                    Text(
                        "Subject: ${topic.subject} • Interval Level ${topic.currentIntervalIndex + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select Your Recall Quality:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    RecallRating.entries.forEach { rating ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedRating == rating) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedRating = rating }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedRating == rating,
                                    onClick = { selectedRating = rating }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(rating.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    val expl = when (rating) {
                                        RecallRating.RECALLED_SUCCESSFULLY -> "Advances to next spaced interval"
                                        RecallRating.RECALLED_WITH_HINTS -> "Repeats in 2 days to reinforce"
                                        RecallRating.NEEDED_RELEARNING -> "Resets interval back to Day 1"
                                    }
                                    Text(expl, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reviewNotes,
                        onValueChange = { reviewNotes = it },
                        label = { Text("Recall Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordRevisionAttempt(topic, selectedRating, reviewNotes)
                        topicToReview = null
                    }
                ) {
                    Text("Submit Result")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToReview = null }) { Text("Cancel") }
            }
        )
    }

    // ADD TOPIC DIALOG
    if (showAddTopicDialog) {
        var title by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("New Learning Topic") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Topic Title (e.g. Raft Consensus, Docker Internals)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. Distributed Systems, CS, Cloud)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Spaced repetition sequence will automatically schedule review for 1, 4, and 7 days after today.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addLearningTopic(title.trim(), subject.ifBlank { "General" }.trim())
                            showAddTopicDialog = false
                        }
                    }
                ) {
                    Text("Add Topic")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun RevisionTopicCard(
    topic: LearningTopic,
    isDue: Boolean,
    onReview: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(topic.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(topic.subject, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "Next: ${DateUtils.formatDate(topic.nextRevisionDate)}",
                        fontSize = 11.sp,
                        color = if (isDue) MomentumAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isDue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDue) {
                    Button(
                        onClick = onReview,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Review", fontSize = 12.sp)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
