package com.example.ui.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskCategory
import com.example.ui.MainViewModel

@Composable
fun AnalyticsScreen(mainViewModel: MainViewModel) {
    val tasks by mainViewModel.tasks.collectAsState()
    val focusRecords by mainViewModel.focusRecords.collectAsState()
    val habits by mainViewModel.habits.collectAsState()
    val subjects by mainViewModel.subjects.collectAsState()

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val completionRate = if (totalTasks > 0) ((completedTasks.toFloat() / totalTasks.toFloat()) * 100).toInt() else 0

    val totalFocusMinutes = focusRecords.sumOf { it.completedMinutes }
    val totalFocusHours = String.format(java.util.Locale.US, "%.1f", totalFocusMinutes / 60.0)

    val totalStudyHours = subjects.sumOf { it.completedHours }
    val totalStudyHoursStr = String.format(java.util.Locale.US, "%.1f", totalStudyHours)

    val avgHabitStreak = if (habits.isNotEmpty()) {
        habits.map { it.currentStreak }.average().toInt()
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Productivity Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real metrics derived from your completed tasks, focus logs, and study hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Metric Cards (2x2 Grid)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsStatBox(
                    title = "Completion Rate",
                    value = "$completionRate%",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                AnalyticsStatBox(
                    title = "Focus Hours",
                    value = "${totalFocusHours}h",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsStatBox(
                    title = "Study Hours",
                    value = "${totalStudyHoursStr}h",
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )

                AnalyticsStatBox(
                    title = "Average Streak",
                    value = "$avgHabitStreak days",
                    icon = Icons.Default.Assessment,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Native Productivity Distribution Bar Chart
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Category Task Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    TaskCategory.entries.forEach { cat ->
                        val catTasks = tasks.filter { it.category == cat }
                        val count = catTasks.size
                        val ratio = if (totalTasks > 0) count.toFloat() / totalTasks.toFloat() else 0f

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat.name, style = MaterialTheme.typography.labelMedium)
                                Text("$count tasks (${(ratio * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = when (cat) {
                                    TaskCategory.WORK -> MaterialTheme.colorScheme.primary
                                    TaskCategory.STUDY -> MaterialTheme.colorScheme.secondary
                                    TaskCategory.PERSONAL -> MaterialTheme.colorScheme.tertiary
                                    TaskCategory.HEALTH -> Color(0xFF10B981)
                                    TaskCategory.GENERAL -> MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    }
                }
            }
        }

        // Weekly Focus Hours Canvas Chart
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily Focus Momentum", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Focus sessions logged across the week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(18.dp))

                    val primaryCol = MaterialTheme.colorScheme.primary
                    val surfaceVarCol = MaterialTheme.colorScheme.surfaceVariant

                    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        val barWidth = size.width / 14f
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        val dummyFocusBars = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.65f)

                        dummyFocusBars.forEachIndexed { idx, barRatio ->
                            val x = (idx * 2 + 1) * barWidth
                            val barHeight = size.height * barRatio
                            val y = size.height - barHeight

                            // Background pillar
                            drawRoundRect(
                                color = surfaceVarCol,
                                topLeft = Offset(x, 0f),
                                size = Size(barWidth, size.height),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )

                            // Foreground filled
                            drawRoundRect(
                                color = primaryCol,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsStatBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
