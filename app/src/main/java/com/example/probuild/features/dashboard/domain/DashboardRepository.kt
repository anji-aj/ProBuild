package com.example.probuild.features.dashboard.domain

import com.example.probuild.features.projects.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getRecentProjects(): Flow<List<Project>>
}
