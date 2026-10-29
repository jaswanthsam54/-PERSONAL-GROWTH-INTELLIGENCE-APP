package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MomentumViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MomentumViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showCsvPreviewDialog by remember { mutableStateOf<String?>(null) }
    var showDemoDataSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Data", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DATA MANAGEMENT CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Data Export & Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Momentum is strictly local-first. All records, reflections, and metrics stay private on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val csv = viewModel.getExportCsv()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Momentum Data Export", csv)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    showCsvPreviewDialog = csv
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("export_csv_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export All Personal Data (CSV)", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // SAMPLE DATA SEEDING (TEST DRIVE)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Demo Data for Testing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Populate realistic sample tasks, focus sessions, spaced revisions, and reflection logs to test-drive all analytics charts, heatmaps, and rule-based insights.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.seedDemoData()
                                showDemoDataSuccessDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("seed_demo_data_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Realistic Demo Records", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // DANGER ZONE / CLEAR ALL DATA
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Danger Zone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MomentumRose)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Irreversibly erase all stored goals, tasks, focus sessions, revision logs, and reflection records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showClearDataDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MomentumRose),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("clear_all_data_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // APP ARCHITECTURE & ABOUT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("About Momentum", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Version: 1.0.0 (Local-First Architecture)\n" +
                                    "Database: SQLite via Android Jetpack Room (v2.7.0)\n" +
                                    "UI Framework: Kotlin Jetpack Compose (Material Design 3)\n" +
                                    "Intelligence: Deterministic rule-based analytical engine\n" +
                                    "Loop: Plan → Execute → Record → Analyze → Improve",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }

    // CONFIRM CLEAR ALL DATA DIALOG
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Stored Records?") },
            text = {
                Text("This action cannot be undone. All tasks, goals, focus records, spaced revisions, and reflections will be deleted permanently.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                        Toast.makeText(context, "All data cleared successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            }
        )
    }

    // CSV PREVIEW DIALOG
    if (showCsvPreviewDialog != null) {
        AlertDialog(
            onDismissRequest = { showCsvPreviewDialog = null },
            title = { Text("CSV Export Ready") },
            text = {
                Column {
                    Text("The data has been copied to your clipboard. Here is a preview snippet:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = showCsvPreviewDialog!!.take(800) + if (showCsvPreviewDialog!!.length > 800) "\n... [truncated]" else "",
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCsvPreviewDialog = null }) { Text("Done") }
            }
        )
    }

    // DEMO DATA SUCCESS DIALOG
    if (showDemoDataSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDemoDataSuccessDialog = false },
            title = { Text("Demo Records Loaded!") },
            text = {
                Text("Sample goals, tasks, focus sessions, spaced repetition logs, and reflection records have been inserted into SQLite. Explore the Dashboard, Planner, Insights, and Review tabs to see the calculations and charts in action!")
            },
            confirmButton = {
                Button(onClick = { showDemoDataSuccessDialog = false }) { Text("Explore App") }
            }
        )
    }
}
