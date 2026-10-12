package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        CalendarEventEntity::class,
        FocusRecordEntity::class,
        HabitEntity::class,
        GoalEntity::class,
        StudySubjectEntity::class,
        StudyTopicEntity::class,
        NoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TimeflowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun calendarDao(): CalendarDao
    abstract fun focusDao(): FocusDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun studyDao(): StudyDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: TimeflowDatabase? = null

        fun getInstance(context: Context): TimeflowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeflowDatabase::class.java,
                    "timeflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
