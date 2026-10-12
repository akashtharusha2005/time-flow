package com.example.data.repository

import android.content.Context
import com.example.data.local.Converters
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.local.TimeflowDatabase
import com.example.data.model.Priority
import com.example.data.model.RecurrenceType
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TaskRepository(
    private val context: Context,
    private val taskDao: TaskDao = TimeflowDatabase.getInstance(context).taskDao()
) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)

    fun getTasks(userId: String): Flow<List<TaskItem>> {
        return taskDao.getTasks(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun saveTask(task: TaskItem) {
        val taskId = if (task.id.isBlank()) UUID.randomUUID().toString() else task.id
        val finalTask = task.copy(
            id = taskId,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.insertTask(finalTask.toEntity())

        // Try syncing to Firestore in background
        if (!finalTask.userId.startsWith("demo_guest_")) {
            ioScope.launch {
                try {
                    val fs = firestore ?: return@launch
                    val map = hashMapOf<String, Any>(
                        "id" to finalTask.id,
                        "userId" to finalTask.userId,
                        "title" to finalTask.title,
                        "description" to finalTask.description,
                        "priority" to finalTask.priority.name,
                        "category" to finalTask.category.name,
                        "tags" to finalTask.tags,
                        "isCompleted" to finalTask.isCompleted,
                        "estimatedMinutes" to finalTask.estimatedMinutes,
                        "actualMinutes" to finalTask.actualMinutes,
                        "recurrence" to finalTask.recurrence.name,
                        "notes" to finalTask.notes,
                        "updatedAt" to finalTask.updatedAt
                    )
                    finalTask.dueDateMillis?.let { map["dueDateMillis"] = it }
                    finalTask.startTime?.let { map["startTime"] = it }
                    finalTask.endTime?.let { map["endTime"] = it }

                    fs.collection("users")
                        .document(finalTask.userId)
                        .collection("tasks")
                        .document(finalTask.id)
                        .set(map, SetOptions.merge())
                        .await()
                    taskDao.updateTask(finalTask.copy(isSynced = true).toEntity())
                } catch (_: Exception) {
                    // Safe offline queue
                }
            }
        }
    }

    suspend fun toggleTaskCompletion(task: TaskItem) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            updatedAt = System.currentTimeMillis()
        )
        saveTask(updated)
    }

    suspend fun deleteTask(userId: String, id: String) {
        taskDao.deleteTask(userId, id)
        if (!userId.startsWith("demo_guest_")) {
            ioScope.launch {
                try {
                    firestore?.collection("users")
                        ?.document(userId)
                        ?.collection("tasks")
                        ?.document(id)
                        ?.delete()
                        ?.await()
                } catch (_: Exception) {}
            }
        }
    }

    private fun TaskEntity.toModel(): TaskItem {
        return TaskItem(
            id = id,
            userId = userId,
            title = title,
            description = description,
            priority = runCatching { Priority.valueOf(priority) }.getOrDefault(Priority.MEDIUM),
            category = runCatching { TaskCategory.valueOf(category) }.getOrDefault(TaskCategory.GENERAL),
            tags = Converters.stringToList(tags),
            dueDateMillis = dueDateMillis,
            startTime = startTime,
            endTime = endTime,
            estimatedMinutes = estimatedMinutes,
            actualMinutes = actualMinutes,
            isCompleted = isCompleted,
            subtasks = Converters.jsonToSubtasks(subtasksJson),
            notes = notes,
            recurrence = runCatching { RecurrenceType.valueOf(recurrence) }.getOrDefault(RecurrenceType.NONE),
            reminderMinutesBefore = reminderMinutesBefore,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }

    private fun TaskItem.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            userId = userId,
            title = title,
            description = description,
            priority = priority.name,
            category = category.name,
            tags = Converters.listToString(tags),
            dueDateMillis = dueDateMillis,
            startTime = startTime,
            endTime = endTime,
            estimatedMinutes = estimatedMinutes,
            actualMinutes = actualMinutes,
            isCompleted = isCompleted,
            subtasksJson = Converters.subtasksToJson(subtasks),
            notes = notes,
            recurrence = recurrence.name,
            reminderMinutesBefore = reminderMinutesBefore,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}
