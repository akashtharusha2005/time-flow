package com.example.data.repository

import android.content.Context
import com.example.data.local.Converters
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.local.TimeflowDatabase
import com.example.data.model.NoteItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NoteRepository(
    private val context: Context,
    private val noteDao: NoteDao = TimeflowDatabase.getInstance(context).noteDao()
) {
    fun getNotes(userId: String): Flow<List<NoteItem>> = noteDao.getNotes(userId).map { list ->
        list.map { it.toModel() }
    }

    suspend fun saveNote(note: NoteItem) {
        val id = if (note.id.isBlank()) UUID.randomUUID().toString() else note.id
        noteDao.insertNote(note.copy(id = id, updatedAt = System.currentTimeMillis()).toEntity())
    }

    suspend fun togglePinNote(note: NoteItem) {
        saveNote(note.copy(isPinned = !note.isPinned))
    }

    suspend fun deleteNote(userId: String, id: String) = noteDao.deleteNote(userId, id)

    private fun NoteEntity.toModel(): NoteItem = NoteItem(
        id = id,
        userId = userId,
        title = title,
        content = content,
        category = category,
        tags = Converters.stringToList(tags),
        isPinned = isPinned,
        taskId = taskId,
        updatedAt = updatedAt
    )

    private fun NoteItem.toEntity(): NoteEntity = NoteEntity(
        id = id,
        userId = userId,
        title = title,
        content = content,
        category = category,
        tags = Converters.listToString(tags),
        isPinned = isPinned,
        taskId = taskId,
        updatedAt = updatedAt
    )
}
