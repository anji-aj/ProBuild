package com.example.probuild.features.settings.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getActiveProjectName(): Flow<String?>
    fun getActiveProjectId(): Flow<String?>
    fun getUnitSystem(): Flow<String>
    fun getNotificationsEnabled(): Flow<Boolean>
    fun getDailyReminderEnabled(): Flow<Boolean>
    fun getThemeMode(): Flow<String>
    suspend fun setUnitSystem(unit: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDailyReminderEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun clearActiveProjectCalculationHistory()
    suspend fun resetAllPreferences()
}
