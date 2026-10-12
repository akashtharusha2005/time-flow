package com.example.data.repository

import android.content.Context
import com.example.data.local.StudyDao
import com.example.data.local.StudySubjectEntity
import com.example.data.local.StudyTopicEntity
import com.example.data.local.TimeflowDatabase
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class StudyRepository(
    private val context: Context,
    private val studyDao: StudyDao = TimeflowDatabase.getInstance(context).studyDao()
) {
    fun getSubjects(userId: String): Flow<List<StudySubject>> = studyDao.getSubjects(userId).map { list ->
        list.map { it.toModel() }
    }

    fun getTopics(userId: String): Flow<List<StudyTopic>> = studyDao.getTopics(userId).map { list ->
        list.map { it.toModel() }
    }

    suspend fun saveSubject(subject: StudySubject) {
        val id = if (subject.id.isBlank()) UUID.randomUUID().toString() else subject.id
        studyDao.insertSubject(subject.copy(id = id).toEntity())
    }

    suspend fun deleteSubject(userId: String, id: String) = studyDao.deleteSubject(userId, id)

    suspend fun saveTopic(topic: StudyTopic) {
        val id = if (topic.id.isBlank()) UUID.randomUUID().toString() else topic.id
        studyDao.insertTopic(topic.copy(id = id).toEntity())
    }

    suspend fun toggleTopicCompletion(topic: StudyTopic) {
        saveTopic(topic.copy(isCompleted = !topic.isCompleted))
    }

    suspend fun deleteTopic(userId: String, id: String) = studyDao.deleteTopic(userId, id)

    suspend fun addStudyHours(subject: StudySubject, hours: Double) {
        saveSubject(subject.copy(completedHours = subject.completedHours + hours))
    }

    private fun StudySubjectEntity.toModel(): StudySubject = StudySubject(
        id = id,
        userId = userId,
        name = name,
        nameSinhala = nameSinhala,
        examDateMillis = examDateMillis,
        targetHours = targetHours,
        completedHours = completedHours,
        colorHex = colorHex,
        notes = notes
    )

    private fun StudySubject.toEntity(): StudySubjectEntity = StudySubjectEntity(
        id = id,
        userId = userId,
        name = name,
        nameSinhala = nameSinhala,
        examDateMillis = examDateMillis,
        targetHours = targetHours,
        completedHours = completedHours,
        colorHex = colorHex,
        notes = notes
    )

    private fun StudyTopicEntity.toModel(): StudyTopic = StudyTopic(
        id = id,
        subjectId = subjectId,
        userId = userId,
        title = title,
        isCompleted = isCompleted,
        difficulty = difficulty
    )

    private fun StudyTopic.toEntity(): StudyTopicEntity = StudyTopicEntity(
        id = id,
        subjectId = subjectId,
        userId = userId,
        title = title,
        isCompleted = isCompleted,
        difficulty = difficulty
    )
}
