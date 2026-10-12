package com.example.data.repository

import android.content.Context
import com.example.data.local.FocusDao
import com.example.data.local.FocusRecordEntity
import com.example.data.local.TimeflowDatabase
import com.example.data.model.FocusRecord
import com.example.data.model.FocusSessionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FocusRepository(
    private val context: Context,
    private val focusDao: FocusDao = TimeflowDatabase.getInstance(context).focusDao()
) {
    fun getFocusRecords(userId: String): Flow<List<FocusRecord>> {
        return focusDao.getFocusRecords(userId).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun recordSession(
        userId: String,
        taskId: String?,
        taskTitle: String?,
        type: FocusSessionType,
        durationMinutes: Int,
        completedMinutes: Int
    ) {
        val record = FocusRecord(
            id = UUID.randomUUID().toString(),
            userId = userId,
            taskId = taskId,
            taskTitle = taskTitle,
            type = type,
            durationMinutes = durationMinutes,
            completedMinutes = completedMinutes,
            timestamp = System.currentTimeMillis()
        )
        focusDao.insertRecord(record.toEntity())
    }

    private fun FocusRecordEntity.toModel(): FocusRecord = FocusRecord(
        id = id,
        userId = userId,
        taskId = taskId,
        taskTitle = taskTitle,
        type = runCatching { FocusSessionType.valueOf(type) }.getOrDefault(FocusSessionType.POMODORO),
        durationMinutes = durationMinutes,
        completedMinutes = completedMinutes,
        timestamp = timestamp
    )

    private fun FocusRecord.toEntity(): FocusRecordEntity = FocusRecordEntity(
        id = id,
        userId = userId,
        taskId = taskId,
        taskTitle = taskTitle,
        type = type.name,
        durationMinutes = durationMinutes,
        completedMinutes = completedMinutes,
        timestamp = timestamp
    )
}
