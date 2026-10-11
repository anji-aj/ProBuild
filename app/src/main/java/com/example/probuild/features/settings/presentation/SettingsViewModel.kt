package com.example.probuild.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.features.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            repository.getActiveProjectName(),
            repository.getActiveProjectId(),
            repository.getUnitSystem()
        ) { name, id, unit -> Triple(name, id, unit) },
        combine(
            repository.getNotificationsEnabled(),
            repository.getDailyReminderEnabled(),
            repository.getThemeMode()
        ) { notifs, reminders, theme -> Triple(notifs, reminders, theme) },
        _userMessage
    ) { (name, id, unit), (notifs, reminders, theme), msg ->
        SettingsUiState(
            activeProjectName = name,
            activeProjectId = id,
            unitSystem = unit,
            notificationsEnabled = notifs,
            dailyReminderEnabled = reminders,
            themeMode = theme,
            appVersion = "1.0.0",
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setUnitSystem(unit: String) {
        viewModelScope.launch {
            repository.setUnitSystem(unit)
            _userMessage.value = "Unit system updated to $unit"
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            _userMessage.value = if (enabled) "Notifications enabled" else "Notifications disabled"
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDailyReminderEnabled(enabled)
            _userMessage.value = if (enabled) "Daily site diary reminder enabled" else "Daily site diary reminder disabled"
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
            _userMessage.value = "Theme updated to ${mode.lowercase().replaceFirstChar { it.uppercase() }}"
        }
    }

    fun clearActiveProjectCalculations() {
        viewModelScope.launch {
            repository.clearActiveProjectCalculationHistory()
            _userMessage.value = "Active project calculation history cleared"
        }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            repository.resetAllPreferences()
            _userMessage.value = "Preferences reset to default"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
