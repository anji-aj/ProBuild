package com.example.probuild.features.projects.domain

import com.example.probuild.features.projects.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectsRepository {
    fun getProjects(): Flow<List<Project>>
    suspend fun getProjectById(id: String): Project?
    suspend fun insertProject(project: Project)
    suspend fun updateProject(project: Project)
    suspend fun deleteProject(id: String)
    suspend fun toggleFavorite(id: String)
}
