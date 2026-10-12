package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "timeflow_preferences")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AppLanguage { EN, SI }

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.EN,
    val isAppLockEnabled: Boolean = false,
    val appLockPin: String = "",
    val pomodoroMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val dailyFocusGoalMinutes: Int = 120,
    val dailyStudyGoalHours: Double = 2.0
)

class PreferencesManager(private val context: Context) {
    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_LANG = stringPreferencesKey("app_language")
        private val KEY_APP_LOCK = booleanPreferencesKey("app_lock_enabled")
        private val KEY_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        private val KEY_POMODORO = intPreferencesKey("pomodoro_minutes")
        private val KEY_SHORT_BREAK = intPreferencesKey("short_break_minutes")
        private val KEY_LONG_BREAK = intPreferencesKey("long_break_minutes")
        private val KEY_FOCUS_GOAL = intPreferencesKey("focus_goal_minutes")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name
        val langStr = prefs[KEY_LANG] ?: AppLanguage.EN.name
        AppPreferences(
            themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM),
            language = runCatching { AppLanguage.valueOf(langStr) }.getOrDefault(AppLanguage.EN),
            isAppLockEnabled = prefs[KEY_APP_LOCK] ?: false,
            appLockPin = prefs[KEY_LOCK_PIN] ?: "",
            pomodoroMinutes = prefs[KEY_POMODORO] ?: 25,
            shortBreakMinutes = prefs[KEY_SHORT_BREAK] ?: 5,
            longBreakMinutes = prefs[KEY_LONG_BREAK] ?: 15,
            dailyFocusGoalMinutes = prefs[KEY_FOCUS_GOAL] ?: 120
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[KEY_LANG] = language.name }
    }

    suspend fun setAppLock(enabled: Boolean, pin: String) {
        context.dataStore.edit {
            it[KEY_APP_LOCK] = enabled
            it[KEY_LOCK_PIN] = pin
        }
    }

    suspend fun updateTimerSettings(pomo: Int, shortBreak: Int, longBreak: Int) {
        context.dataStore.edit {
            it[KEY_POMODORO] = pomo
            it[KEY_SHORT_BREAK] = shortBreak
            it[KEY_LONG_BREAK] = longBreak
        }
    }
}
