package com.example.probuild.features.projects.presentation

import com.example.probuild.features.projects.domain.model.Project

data class ProjectsUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val filterStatus: Set<com.example.probuild.features.projects.domain.model.ProjectStatus> = emptySet(),
    val errorMessage: Int? = null
)

enum class SortOrder {
    NEWEST, OLDEST, BUDGET_HIGH_LOW, ALPHABETICAL, PROGRESS_HIGH_LOW
}
