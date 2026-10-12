package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getTaskById(userId: String, id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id AND userId = :userId")
    suspend fun deleteTask(userId: String, id: String)

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE userId = :userId ORDER BY startTimeMillis ASC")
    fun getEvents(userId: String): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE id = :id AND userId = :userId")
    suspend fun deleteEvent(userId: String, id: String)

    @Query("DELETE FROM calendar_events WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getFocusRecords(userId: String): Flow<List<FocusRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FocusRecordEntity)

    @Query("DELETE FROM focus_records WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getHabits(userId: String): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id AND userId = :userId")
    suspend fun deleteHabit(userId: String, id: String)

    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoals(userId: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id AND userId = :userId")
    suspend fun deleteGoal(userId: String, id: String)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_subjects WHERE userId = :userId ORDER BY name ASC")
    fun getSubjects(userId: String): Flow<List<StudySubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: StudySubjectEntity)

    @Update
    suspend fun updateSubject(subject: StudySubjectEntity)

    @Query("DELETE FROM study_subjects WHERE id = :id AND userId = :userId")
    suspend fun deleteSubject(userId: String, id: String)

    @Query("SELECT * FROM study_topics WHERE userId = :userId")
    fun getTopics(userId: String): Flow<List<StudyTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: StudyTopicEntity)

    @Update
    suspend fun updateTopic(topic: StudyTopicEntity)

    @Query("DELETE FROM study_topics WHERE id = :id AND userId = :userId")
    suspend fun deleteTopic(userId: String, id: String)

    @Query("DELETE FROM study_subjects WHERE userId = :userId")
    suspend fun deleteAllSubjectsForUser(userId: String)

    @Query("DELETE FROM study_topics WHERE userId = :userId")
    suspend fun deleteAllTopicsForUser(userId: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotes(userId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id AND userId = :userId")
    suspend fun deleteNote(userId: String, id: String)

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
