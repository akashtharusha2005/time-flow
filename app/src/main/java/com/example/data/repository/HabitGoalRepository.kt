package com.example.data.repository

import android.content.Context
import com.example.data.local.GoalDao
import com.example.data.local.GoalEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.TimeflowDatabase
import com.example.data.model.GoalItem
import com.example.data.model.HabitItem
import com.example.data.model.TaskCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HabitGoalRepository(
    private val context: Context,
    private val habitDao: HabitDao = TimeflowDatabase.getInstance(context).habitDao(),
    private val goalDao: GoalDao = TimeflowDatabase.getInstance(context).goalDao()
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getHabits(userId: String): Flow<List<HabitItem>> = habitDao.getHabits(userId).map { list ->
        list.map { it.toModel() }
    }

    fun getGoals(userId: String): Flow<List<GoalItem>> = goalDao.getGoals(userId).map { list ->
        list.map { it.toModel() }
    }

    suspend fun saveHabit(habit: HabitItem) {
        val id = if (habit.id.isBlank()) UUID.randomUUID().toString() else habit.id
        habitDao.insertHabit(habit.copy(id = id).toEntity())
    }

    suspend fun toggleHabitToday(habit: HabitItem) {
        val todayStr = dateFormat.format(Date())
        val completedDates = habit.completedDates.toMutableList()
        val isDoneToday = completedDates.contains(todayStr)

        val newStreak: Int
        val newBest: Int

        if (isDoneToday) {
            completedDates.remove(todayStr)
            newStreak = (habit.currentStreak - 1).coerceAtLeast(0)
            newBest = habit.bestStreak
        } else {
            completedDates.add(todayStr)
            newStreak = habit.currentStreak + 1
            newBest = maxOf(newStreak, habit.bestStreak)
        }

        saveHabit(
            habit.copy(
                completedDates = completedDates,
                currentStreak = newStreak,
                bestStreak = newBest
            )
        )
    }

    suspend fun deleteHabit(userId: String, id: String) = habitDao.deleteHabit(userId, id)

    suspend fun saveGoal(goal: GoalItem) {
        val id = if (goal.id.isBlank()) UUID.randomUUID().toString() else goal.id
        goalDao.insertGoal(goal.copy(id = id).toEntity())
    }

    suspend fun updateGoalProgress(goal: GoalItem, delta: Double) {
        val newVal = (goal.currentValue + delta).coerceAtLeast(0.0)
        val isCompleted = newVal >= goal.targetValue
        saveGoal(goal.copy(currentValue = newVal, isCompleted = isCompleted))
    }

    suspend fun deleteGoal(userId: String, id: String) = goalDao.deleteGoal(userId, id)

    private fun HabitEntity.toModel(): HabitItem {
        val dates = mutableListOf<String>()
        runCatching {
            val arr = JSONArray(completedDatesJson)
            for (i in 0 until arr.length()) dates.add(arr.getString(i))
        }
        return HabitItem(
            id = id,
            userId = userId,
            title = title,
            description = description,
            iconName = iconName,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completedDates = dates,
            targetDaysPerWeek = targetDaysPerWeek,
            createdAt = createdAt
        )
    }

    private fun HabitItem.toEntity(): HabitEntity {
        val arr = JSONArray()
        completedDates.forEach { arr.put(it) }
        return HabitEntity(
            id = id,
            userId = userId,
            title = title,
            description = description,
            iconName = iconName,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completedDatesJson = arr.toString(),
            targetDaysPerWeek = targetDaysPerWeek,
            createdAt = createdAt
        )
    }

    private fun GoalEntity.toModel(): GoalItem = GoalItem(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = runCatching { TaskCategory.valueOf(category) }.getOrDefault(TaskCategory.GENERAL),
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadlineMillis = deadlineMillis,
        isCompleted = isCompleted,
        createdAt = createdAt
    )

    private fun GoalItem.toEntity(): GoalEntity = GoalEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = category.name,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadlineMillis = deadlineMillis,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}
