package com.example.data.model

data class UserSession(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val isAnonymous: Boolean = false,
    val isDemo: Boolean = false
)

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskCategory {
    WORK, STUDY, PERSONAL, HEALTH, GENERAL
}

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY
}

data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

data class TaskItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.GENERAL,
    val tags: List<String> = emptyList(),
    val dueDateMillis: Long? = null,
    val startTime: String? = null, // e.g. "09:00"
    val endTime: String? = null,   // e.g. "10:30"
    val estimatedMinutes: Int = 30,
    val actualMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val subtasks: List<Subtask> = emptyList(),
    val notes: String = "",
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val reminderMinutesBefore: Int? = 15,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

data class CalendarEvent(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.GENERAL,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis() + 3600000,
    val isAllDay: Boolean = false,
    val colorHex: String = "#0EA5E9",
    val taskId: String? = null
)

enum class FocusSessionType {
    POMODORO, SHORT_BREAK, LONG_BREAK
}

data class FocusRecord(
    val id: String = "",
    val userId: String = "",
    val taskId: String? = null,
    val taskTitle: String? = null,
    val type: FocusSessionType = FocusSessionType.POMODORO,
    val durationMinutes: Int = 25,
    val completedMinutes: Int = 25,
    val timestamp: Long = System.currentTimeMillis()
)

data class HabitItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val iconName: String = "check_circle",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedDates: List<String> = emptyList(), // ISO dates "YYYY-MM-DD"
    val targetDaysPerWeek: Int = 7,
    val createdAt: Long = System.currentTimeMillis()
)

data class GoalItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.GENERAL,
    val targetValue: Double = 100.0,
    val currentValue: Double = 0.0,
    val unit: String = "%",
    val deadlineMillis: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class StudySubject(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val nameSinhala: String? = null,
    val examDateMillis: Long? = null,
    val targetHours: Double = 50.0,
    val completedHours: Double = 0.0,
    val colorHex: String = "#818CF8",
    val notes: String = ""
)

data class StudyTopic(
    val id: String = "",
    val subjectId: String = "",
    val userId: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val difficulty: String = "Medium"
)

data class NoteItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "General",
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val taskId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class AiMessage(
    val id: String = "",
    val isUser: Boolean = true,
    val content: String = "",
    val thinking: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
