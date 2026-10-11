package com.example.probuild.features.settings.data

import com.example.probuild.data.local.CalculationDao
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.data.local.ProjectDao
import com.example.probuild.features.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val projectDao: ProjectDao,
    private val calculationDao: CalculationDao,
    private val preferenceManager: PreferenceManager
) : SettingsRepository {

    override fun getActiveProjectName(): Flow<String?> {
        return preferenceManager.selectedProjectId.map { selectedId ->
            if (selectedId != null) {
                projectDao.getProjectById(selectedId)?.projectName
            } else {
                projectDao.getProjects().firstOrNull()?.firstOrNull()?.projectName
            }
        }
    }

    override fun getActiveProjectId(): Flow<String?> {
        return preferenceManager.selectedProjectId.map { selectedId ->
            selectedId ?: projectDao.getProjects().firstOrNull()?.firstOrNull()?.id
        }
    }

    override fun getUnitSystem(): Flow<String> = preferenceManager.unitSystem

    override fun getNotificationsEnabled(): Flow<Boolean> = preferenceManager.notificationsEnabled

    override fun getDailyReminderEnabled(): Flow<Boolean> = preferenceManager.dailyReminderEnabled

    override fun getThemeMode(): Flow<String> = preferenceManager.themeMode

    override suspend fun setUnitSystem(unit: String) {
        preferenceManager.saveUnitSystem(unit)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferenceManager.saveNotificationsEnabled(enabled)
    }

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        preferenceManager.saveDailyReminderEnabled(enabled)
    }

    override suspend fun setThemeMode(mode: String) {
        preferenceManager.saveThemeMode(mode)
    }

    override suspend fun clearActiveProjectCalculationHistory() {
        val selectedId = preferenceManager.selectedProjectId.firstOrNull()
            ?: projectDao.getProjects().firstOrNull()?.firstOrNull()?.id
        if (selectedId != null) {
            calculationDao.clearHistoryByProject(selectedId)
        }
    }

    override suspend fun resetAllPreferences() {
        preferenceManager.clearAllPreferences()
    }
}
