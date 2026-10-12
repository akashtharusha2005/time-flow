package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun CalendarScreen(mainViewModel: MainViewModel) {
    val events by mainViewModel.events.collectAsState()
    val tasks by mainViewModel.tasks.collectAsState()

    var selectedCalendarMillis by remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        mutableLongStateOf(cal.timeInMillis)
    }

    var isAddEventDialogOpen by remember { mutableStateOf(false) }

    // Calculate dates for current week strip
    val weekDates = remember(selectedCalendarMillis) {
        val list = mutableListOf<Long>()
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedCalendarMillis
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        for (i in 0 until 7) {
            list.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Filter events and tasks for the selected date
    val dayStart = selectedCalendarMillis
    val dayEnd = selectedCalendarMillis + 86400000L

    val dayEvents = remember(events, selectedCalendarMillis) {
        events.filter { event ->
            event.startTimeMillis in dayStart..dayEnd ||
                    (event.startTimeMillis < dayStart && event.endTimeMillis > dayStart)
        }
    }

    val dayTasks = remember(tasks, selectedCalendarMillis) {
        tasks.filter { task ->
            task.dueDateMillis != null && task.dueDateMillis in dayStart..dayEnd
        }
    }

    val monthYearFormat = remember(selectedCalendarMillis) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(selectedCalendarMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthYearFormat,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = selectedCalendarMillis
                            add(Calendar.DAY_OF_YEAR, -7)
                        }
                        selectedCalendarMillis = cal.timeInMillis
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous week")
                    }

                    TextButton(onClick = {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedCalendarMillis = cal.timeInMillis
                    }) {
                        Text("Today")
                    }

                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = selectedCalendarMillis
                            add(Calendar.DAY_OF_YEAR, 7)
                        }
                        selectedCalendarMillis = cal.timeInMillis
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next week")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Week Day Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDates.forEach { dateMillis ->
                    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(dateMillis)).take(3)
                    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                    val isSelected = dateMillis == selectedCalendarMillis

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { selectedCalendarMillis = dateMillis }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayOfWeek,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$dayOfMonth",
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Agenda List for Selected Day
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Section 1: Events & Timeblocks
                item {
                    Text(
                        text = "Scheduled Events & Timeblocks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (dayEvents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "No events scheduled for this day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(dayEvents) { event ->
                        val conflicts = mainViewModel.checkScheduleConflicts(
                            event.startTimeMillis,
                            event.endTimeMillis,
                            event.id
                        )

                        EventCard(
                            event = event,
                            hasConflict = conflicts.isNotEmpty(),
                            onDelete = { mainViewModel.deleteEvent(event.id) }
                        )
                    }
                }

                // Section 2: Tasks Due
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tasks Due on this Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (dayTasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "No deadlines due on this day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(dayTasks) { task ->
                        ElevatedCard(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = task.title, fontWeight = FontWeight.SemiBold)
                                    Text(text = "${task.estimatedMinutes} mins • ${task.priority.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Event FAB
        FloatingActionButton(
            onClick = { isAddEventDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_event_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Event")
        }
    }

    if (isAddEventDialogOpen) {
        AddEventDialog(
            selectedDateMillis = selectedCalendarMillis,
            onDismiss = { isAddEventDialogOpen = false },
            onSave = { event ->
                mainViewModel.saveEvent(event)
                isAddEventDialogOpen = false
            },
            onCheckConflict = { start, end ->
                mainViewModel.checkScheduleConflicts(start, end)
            }
        )
    }
}

@Composable
fun EventCard(
    event: CalendarEvent,
    hasConflict: Boolean,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val startTimeStr = timeFormat.format(Date(event.startTimeMillis))
    val endTimeStr = timeFormat.format(Date(event.endTimeMillis))

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$startTimeStr – $endTimeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (hasConflict) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Conflict", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Schedule Conflict: Overlaps with another event!",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddEventDialog(
    selectedDateMillis: Long,
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onCheckConflict: (Long, Long) -> List<CalendarEvent>
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startHour by remember { mutableIntStateOf(9) }
    var endHour by remember { mutableIntStateOf(10) }

    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val startTime = remember(startHour) {
        cal.clone().let {
            val c = it as Calendar
            c.set(Calendar.HOUR_OF_DAY, startHour)
            c.set(Calendar.MINUTE, 0)
            c.timeInMillis
        }
    }
    val endTime = remember(endHour) {
        cal.clone().let {
            val c = it as Calendar
            c.set(Calendar.HOUR_OF_DAY, endHour)
            c.set(Calendar.MINUTE, 0)
            c.timeInMillis
        }
    }

    val conflicts = remember(startTime, endTime) {
        onCheckConflict(startTime, endTime)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start Hour: ${startHour}:00")
                    Row {
                        IconButton(onClick = { if (startHour > 0) startHour-- }) { Text("-") }
                        IconButton(onClick = { if (startHour < 23) startHour++ }) { Text("+") }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("End Hour: ${endHour}:00")
                    Row {
                        IconButton(onClick = { if (endHour > startHour) endHour-- }) { Text("-") }
                        IconButton(onClick = { if (endHour < 24) endHour++ }) { Text("+") }
                    }
                }

                if (conflicts.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Warning: Conflicts with '${conflicts.first().title}'",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val event = CalendarEvent(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            description = description.trim(),
                            startTimeMillis = startTime,
                            endTimeMillis = endTime
                        )
                        onSave(event)
                    }
                },
                modifier = Modifier.testTag("save_event_button")
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
