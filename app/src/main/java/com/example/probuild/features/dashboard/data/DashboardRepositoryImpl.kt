package com.example.probuild.features.dashboard.data

import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.dashboard.domain.DashboardRepository
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DashboardRepositoryImpl(
    private val projectsRepository: ProjectsRepository = ServiceLocator.projectsRepository
) : DashboardRepository {
    
    override fun getRecentProjects(): Flow<List<Project>> {
        return projectsRepository.getProjects().map { projects ->
            // Take the 3 most recent or updated projects
            projects.sortedByDescending { it.updatedAt }.take(3)
        }
    }
}
