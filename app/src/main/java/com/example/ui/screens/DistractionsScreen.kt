package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.util.DateUtils
import com.example.data.model.Distraction
import com.example.ui.MomentumViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistractionsScreen(
    viewModel: MomentumViewModel,
    onBack: () -> Unit
) {
    val allDistractions by viewModel.allDistractions.collectAsStateWithLifecycle()
    val distractionSummaries by viewModel.distractionSummaries.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalLostMinutes = allDistractions.sumOf { it.durationMinutes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Distraction Journal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("distractions_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("distractions_add_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Log Distraction")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("distractions_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Distraction")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("distractions_screen"),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp)
        ) {
            // STATS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Time Lost",
                        value = "${totalLostMinutes}m",
                        supportingText = "${allDistractions.size} recorded incidents",
                        icon = Icons.Default.TimerOff,
                        valueColor = MomentumAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Top Distraction",
                        value = distractionSummaries.firstOrNull()?.category ?: "None",
                        supportingText = if (distractionSummaries.isNotEmpty()) "${distractionSummaries.first().totalMinutes}m logged" else "Clean focus",
                        icon = Icons.Default.WarningAmber,
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // SUMMARY BY CATEGORY
            if (distractionSummaries.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Distraction Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            distractionSummaries.forEach { summary ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(summary.category, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${summary.totalMinutes}m (${summary.percentageOfTotal.toInt()}%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { summary.percentageOfTotal / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(2.5.dp)),
                                        color = MomentumAmber,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // LOGGED INCIDENTS LIST
            item {
                Text(
                    text = "Recorded Distractions (${allDistractions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (allDistractions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.CheckCircleOutline,
                            title = "No Distractions Recorded",
                            message = "Whenever you feel your attention pulled away, log a 1-tap entry to build awareness."
                        )
                    }
                }
            } else {
                items(allDistractions, key = { it.id }) { item ->
                    DistractionItemCard(
                        distraction = item,
                        onDelete = { viewModel.deleteDistraction(item) }
                    )
                }
            }
        }
    }

    // ADD DISTRACTION DIALOG
    if (showAddDialog) {
        var category by remember { mutableStateOf("phone_use") }
        var duration by remember { mutableStateOf("10") }
        var note by remember { mutableStateOf("") }
        val categories = listOf("phone_use", "notifications", "unclear_task", "fatigue", "environment", "other")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Distraction") },
            text = {
                Column {
                    Text("Category:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.replace('_', ' ').replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Context / Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = duration.toIntOrNull() ?: 5
                        viewModel.recordDistraction(category, mins, note)
                        showAddDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DistractionItemCard(
    distraction: Distraction,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
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
                Text(
                    text = distraction.category.replace('_', ' ').replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (distraction.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = distraction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatDate(DateUtils.dateFromMillis(distraction.occurredAt)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${distraction.durationMinutes}m",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MomentumAmber
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
