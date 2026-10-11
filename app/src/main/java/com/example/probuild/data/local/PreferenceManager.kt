package com.example.probuild.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "probuild_preferences")

class PreferenceManager(private val context: Context) {

    companion object {
        private val SELECTED_PROJECT_ID = stringPreferencesKey("selected_project_id")
        private val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val selectedProjectId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_PROJECT_ID]
        }

    val unitSystem: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[UNIT_SYSTEM] ?: "Metric"
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    val dailyReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DAILY_REMINDER_ENABLED] ?: true
        }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "SYSTEM"
        }

    suspend fun saveSelectedProjectId(id: String?) {
        context.dataStore.edit { preferences ->
            if (id == null) {
                preferences.remove(SELECTED_PROJECT_ID)
            } else {
                preferences[SELECTED_PROJECT_ID] = id
            }
        }
    }

    suspend fun saveUnitSystem(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM] = unit
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
