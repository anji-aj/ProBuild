package com.example.probuild.features.dashboard.presentation

import com.example.probuild.features.dashboard.domain.DashboardAction
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.features.projects.domain.model.Project

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    
    data class Success(
        val greetingRes: Int,
        val actions: List<DashboardAction>,
        val recentProjects: List<Project>,
        val allProjectsCount: Int = 0,
        val activeProjectsCount: Int = 0,
        val completedProjectsCount: Int = 0,
        val searchQuery: String = "",
        val unreadNotificationCount: Int = 0,
        val diarySummary: DiarySummary = DiarySummary()
    ) : DashboardUiState
    
    data object Empty : DashboardUiState
    
    data class Error(val messageRes: Int) : DashboardUiState
}
