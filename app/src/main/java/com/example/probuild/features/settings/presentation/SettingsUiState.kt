package com.example.probuild.features.settings.presentation

data class SettingsUiState(
    val activeProjectName: String? = null,
    val activeProjectId: String? = null,
    val unitSystem: String = "Metric",
    val notificationsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
    val appVersion: String = "1.0.0",
    val userMessage: String? = null
)
