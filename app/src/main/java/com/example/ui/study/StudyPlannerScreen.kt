package com.example.ui.study

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import com.example.ui.MainViewModel
import java.util.UUID

@Composable
fun StudyPlannerScreen(mainViewModel: MainViewModel) {
    val subjects by mainViewModel.subjects.collectAsState()
    val topics by mainViewModel.topics.collectAsState()

    var isAddSubjectDialogOpen by remember { mutableStateOf(false) }
    var subjectToLogHours by remember { mutableStateOf<StudySubject?>(null) }
    var subjectToAddTopic by remember { mutableStateOf<StudySubject?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.study_planner),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track subjects, exam countdowns, and lesson milestones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (subjects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No study subjects added yet", fontWeight = FontWeight.Bold)
                            Text("Tap the + button to add your exam subjects and revision goals.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(subjects, key = { it.id }) { subject ->
                    val subjectTopics = topics.filter { it.subjectId == subject.id }
                    StudySubjectCard(
                        subject = subject,
                        topics = subjectTopics,
                        onAiStudyPlan = { mainViewModel.generateAiStudyPlan(subject) },
                        onLogHours = { subjectToLogHours = subject },
                        onAddTopic = { subjectToAddTopic = subject },
                        onToggleTopic = { topic -> mainViewModel.toggleTopic(topic) },
                        onDeleteSubject = { mainViewModel.deleteSubject(subject.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { isAddSubjectDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_subject_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Subject")
        }
    }

    // Add Subject Dialog
    if (isAddSubjectDialogOpen) {
        AddSubjectDialog(
            onDismiss = { isAddSubjectDialogOpen = false },
            onSave = { subject ->
                mainViewModel.saveSubject(subject)
                isAddSubjectDialogOpen = false
            }
        )
    }

    // Log Hours Dialog
    subjectToLogHours?.let { subject ->
        LogHoursDialog(
            subject = subject,
            onDismiss = { subjectToLogHours = null },
            onSave = { hours ->
                mainViewModel.logStudyHours(subject, hours)
                subjectToLogHours = null
            }
        )
    }

    // Add Topic Dialog
    subjectToAddTopic?.let { subject ->
        AddTopicDialog(
            subjectId = subject.id,
            onDismiss = { subjectToAddTopic = null },
            onSave = { topic ->
                mainViewModel.saveTopic(topic)
                subjectToAddTopic = null
            }
        )
    }
}

@Composable
fun StudySubjectCard(
    subject: StudySubject,
    topics: List<StudyTopic>,
    onAiStudyPlan: () -> Unit,
    onLogHours: () -> Unit,
    onAddTopic: () -> Unit,
    onToggleTopic: (StudyTopic) -> Unit,
    onDeleteSubject: () -> Unit
) {
    val daysRemaining = remember(subject.examDateMillis) {
        subject.examDateMillis?.let {
            val diff = it - System.currentTimeMillis()
            (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        }
    }

    val progress = (subject.completedHours / subject.targetHours.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Subject Title, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = subject.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (subject.nameSinhala != null && subject.nameSinhala.isNotBlank()) {
                            Text(text = subject.nameSinhala, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                IconButton(onClick = onDeleteSubject, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exam Countdown Badge if set
            daysRemaining?.let { days ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⏳ Exam in $days days",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Study Hours Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${subject.completedHours} / ${subject.targetHours} hours ($percentage%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onLogHours, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(32.dp)) {
                        Icon(Icons.Default.MoreTime, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Hours", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onAiStudyPlan,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Plan", fontSize = 11.sp)
                    }
                }
            }

            // Topics Checklist
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Milestone Topics (${topics.count { it.isCompleted }}/${topics.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onAddTopic) {
                    Text("+ Add Topic", fontSize = 11.sp)
                }
            }

            topics.forEach { topic ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = topic.isCompleted,
                        onCheckedChange = { onToggleTopic(topic) },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(onDismiss: () -> Unit, onSave: (StudySubject) -> Unit) {
    var name by remember { mutableStateOf("") }
    var nameSinhala by remember { mutableStateOf("") }
    var targetHours by remember { mutableStateOf("30") }
    var examDaysInAdvance by remember { mutableStateOf("14") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name (English) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameSinhala,
                    onValueChange = { nameSinhala = it },
                    label = { Text("Subject Name (සිංහල - Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    label = { Text("Target Hours Goal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = examDaysInAdvance,
                    onValueChange = { examDaysInAdvance = it },
                    label = { Text("Exam Date (Days from now)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    val days = examDaysInAdvance.toLongOrNull() ?: 14L
                    onSave(
                        StudySubject(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            nameSinhala = nameSinhala.trim().ifBlank { null },
                            targetHours = targetHours.toDoubleOrNull() ?: 30.0,
                            examDateMillis = System.currentTimeMillis() + (days * 86400000L)
                        )
                    )
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun LogHoursDialog(subject: StudySubject, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var hours by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Hours for ${subject.name}") },
        text = {
            Column {
                Text("How many study hours would you like to log?")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Hours (e.g. 1.5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val h = hours.toDoubleOrNull() ?: 1.0
                onSave(h)
            }) {
                Text("Log")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddTopicDialog(subjectId: String, onDismiss: () -> Unit, onSave: (StudyTopic) -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milestone Topic") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Topic / Chapter Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onSave(StudyTopic(id = UUID.randomUUID().toString(), subjectId = subjectId, title = title.trim()))
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
