package com.example.data.repository

import android.content.Context
import com.example.data.local.CalendarDao
import com.example.data.local.CalendarEventEntity
import com.example.data.local.TimeflowDatabase
import com.example.data.model.CalendarEvent
import com.example.data.model.TaskCategory
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

class CalendarRepository(
    private val context: Context,
    private val calendarDao: CalendarDao = TimeflowDatabase.getInstance(context).calendarDao()
) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseFirestore.getInstance() else null
        } catch (_: Exception) { null }
    }
    private val ioScope = CoroutineScope(Dispatchers.IO)

    fun getEvents(userId: String): Flow<List<CalendarEvent>> {
        return calendarDao.getEvents(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun saveEvent(event: CalendarEvent) {
        val eventId = if (event.id.isBlank()) UUID.randomUUID().toString() else event.id
        val finalEvent = event.copy(id = eventId)
        calendarDao.insertEvent(finalEvent.toEntity())

        if (!finalEvent.userId.startsWith("demo_guest_")) {
            ioScope.launch {
                try {
                    val fs = firestore ?: return@launch
                    val map = hashMapOf<String, Any>(
                        "id" to finalEvent.id,
                        "userId" to finalEvent.userId,
                        "title" to finalEvent.title,
                        "description" to finalEvent.description,
                        "category" to finalEvent.category.name,
                        "startTimeMillis" to finalEvent.startTimeMillis,
                        "endTimeMillis" to finalEvent.endTimeMillis,
                        "isAllDay" to finalEvent.isAllDay,
                        "colorHex" to finalEvent.colorHex
                    )
                    finalEvent.taskId?.let { map["taskId"] = it }
                    fs.collection("users")
                        .document(finalEvent.userId)
                        .collection("events")
                        .document(finalEvent.id)
                        .set(map, SetOptions.merge())
                        .await()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun deleteEvent(userId: String, id: String) {
        calendarDao.deleteEvent(userId, id)
        if (!userId.startsWith("demo_guest_")) {
            ioScope.launch {
                try {
                    firestore?.collection("users")
                        ?.document(userId)
                        ?.collection("events")
                        ?.document(id)
                        ?.delete()
                        ?.await()
                } catch (_: Exception) {}
            }
        }
    }

    // Schedule conflict detector: returns conflicting events for a given time window
    fun checkConflicts(existingEvents: List<CalendarEvent>, newStart: Long, newEnd: Long, excludeEventId: String? = null): List<CalendarEvent> {
        return existingEvents.filter { event ->
            if (event.id == excludeEventId || event.isAllDay) return@filter false
            // Overlap condition: start < otherEnd and end > otherStart
            newStart < event.endTimeMillis && newEnd > event.startTimeMillis
        }
    }

    private fun CalendarEventEntity.toModel(): CalendarEvent = CalendarEvent(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = runCatching { TaskCategory.valueOf(category) }.getOrDefault(TaskCategory.GENERAL),
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        isAllDay = isAllDay,
        colorHex = colorHex,
        taskId = taskId
    )

    private fun CalendarEvent.toEntity(): CalendarEventEntity = CalendarEventEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = category.name,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        isAllDay = isAllDay,
        colorHex = colorHex,
        taskId = taskId
    )
}
