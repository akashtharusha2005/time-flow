package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.FocusSessionType
import com.example.data.model.Priority
import com.example.data.model.RecurrenceType
import com.example.data.model.Subtask
import com.example.data.model.TaskCategory
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val priority: String,
    val category: String,
    val tags: String, // comma-separated or json
    val dueDateMillis: Long?,
    val startTime: String?,
    val endTime: String?,
    val estimatedMinutes: Int,
    val actualMinutes: Int,
    val isCompleted: Boolean,
    val subtasksJson: String,
    val notes: String,
    val recurrence: String,
    val reminderMinutesBefore: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean,
    val colorHex: String,
    val taskId: String?
)

@Entity(tableName = "focus_records")
data class FocusRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val taskId: String?,
    val taskTitle: String?,
    val type: String,
    val durationMinutes: Int,
    val completedMinutes: Int,
    val timestamp: Long
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val iconName: String,
    val currentStreak: Int,
    val bestStreak: Int,
    val completedDatesJson: String,
    val targetDaysPerWeek: Int,
    val createdAt: Long
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val deadlineMillis: Long?,
    val isCompleted: Boolean,
    val createdAt: Long
)

@Entity(tableName = "study_subjects")
data class StudySubjectEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val nameSinhala: String?,
    val examDateMillis: Long?,
    val targetHours: Double,
    val completedHours: Double,
    val colorHex: String,
    val notes: String
)

@Entity(tableName = "study_topics")
data class StudyTopicEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val userId: String,
    val title: String,
    val isCompleted: Boolean,
    val difficulty: String
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val category: String,
    val tags: String,
    val isPinned: Boolean,
    val taskId: String?,
    val updatedAt: Long
)

object Converters {
    fun subtasksToJson(subtasks: List<Subtask>): String {
        val array = JSONArray()
        subtasks.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("isCompleted", it.isCompleted)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToSubtasks(json: String?): List<Subtask> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Subtask>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Subtask(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        isCompleted = obj.optBoolean("isCompleted")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun listToString(list: List<String>): String = list.joinToString("||")
    fun stringToList(str: String?): List<String> = if (str.isNullOrBlank()) emptyList() else str.split("||").filter { it.isNotBlank() }
}
