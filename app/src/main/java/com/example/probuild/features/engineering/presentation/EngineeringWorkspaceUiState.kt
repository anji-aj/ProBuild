package com.example.probuild.features.engineering.presentation

import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.projects.domain.model.Project

data class EngineeringWorkspaceUiState(
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val history: List<Calculation> = emptyList(),
    val favorites: Set<CalculationType> = emptySet(),
    val isLoading: Boolean = true
)
