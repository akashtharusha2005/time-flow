package com.example.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusRecord
import com.example.data.model.FocusSessionType
import com.example.data.model.TaskItem
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusTimerScreen(mainViewModel: MainViewModel) {
    val timerState by mainViewModel.timerState.collectAsState()
    val tasks by mainViewModel.tasks.collectAsState()
    val focusRecords by mainViewModel.focusRecords.collectAsState()

    var showTaskPicker by remember { mutableStateOf(false) }

    val remainingMinutes = timerState.timeRemainingSeconds / 60
    val remainingSeconds = timerState.timeRemainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", remainingMinutes, remainingSeconds)

    val progress = remember(timerState.timeRemainingSeconds, timerState.totalDurationSeconds) {
        if (timerState.totalDurationSeconds > 0) {
            timerState.timeRemainingSeconds.toFloat() / timerState.totalDurationSeconds.toFloat()
        } else 0f
    }

    val typeColor = when (timerState.currentType) {
        FocusSessionType.POMODORO -> MaterialTheme.colorScheme.primary
        FocusSessionType.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
        FocusSessionType.LONG_BREAK -> MaterialTheme.colorScheme.secondary
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Pills
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = timerState.currentType == FocusSessionType.POMODORO,
                    onClick = { mainViewModel.setFocusSessionType(FocusSessionType.POMODORO) },
                    label = { Text("Pomodoro (25m)") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = timerState.currentType == FocusSessionType.SHORT_BREAK,
                    onClick = { mainViewModel.setFocusSessionType(FocusSessionType.SHORT_BREAK) },
                    label = { Text("Short Break (5m)") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = timerState.currentType == FocusSessionType.LONG_BREAK,
                    onClick = { mainViewModel.setFocusSessionType(FocusSessionType.LONG_BREAK) },
                    label = { Text("Long Break (15m)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Selected Task Pill
        item {
            Card(
                onClick = { showTaskPicker = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timerState.selectedTask?.title ?: "Attach a task to this session (Optional)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Circular Timer Visualizer
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .padding(12.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )

                // Active Progress
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = typeColor,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = timerState.currentType.name.replace("_", " "),
                        style = MaterialTheme.typography.labelMedium,
                        color = typeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Timer Controls
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { mainViewModel.resetTimer() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(24.dp))
                }

                Button(
                    onClick = {
                        if (timerState.isRunning) {
                            mainViewModel.pauseTimer()
                        } else {
                            mainViewModel.startTimer()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = typeColor),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("timer_toggle_button")
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState.isRunning) "Pause" else "Start",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Focus Session History
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${focusRecords.size} sessions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (focusRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Complete your first focus session to start building your momentum!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(focusRecords.take(5)) { record ->
                FocusRecordItem(record = record)
            }
        }
    }

    // Task Selector Dialog
    if (showTaskPicker) {
        val pendingTasks = tasks.filter { !it.isCompleted }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            title = { Text("Select Task for Focus") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        TextButton(
                            onClick = {
                                mainViewModel.selectFocusTask(null)
                                showTaskPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("None (General Focus)")
                        }
                    }
                    items(pendingTasks) { task ->
                        TextButton(
                            onClick = {
                                mainViewModel.selectFocusTask(task)
                                showTaskPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(task.title, maxLines = 1)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTaskPicker = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun FocusRecordItem(record: FocusRecord) {
    val dateStr = remember(record.timestamp) {
        SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date(record.timestamp))
    }

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.taskTitle ?: "General Focus",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${record.completedMinutes} mins",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
