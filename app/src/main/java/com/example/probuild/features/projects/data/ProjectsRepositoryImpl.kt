package com.example.probuild.features.projects.data

import com.example.probuild.data.local.ProjectDao
import com.example.probuild.features.projects.data.mapper.toDomain
import com.example.probuild.features.projects.data.mapper.toEntity
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectsRepositoryImpl(
    private val dao: ProjectDao
) : ProjectsRepository {

    override fun getProjects(): Flow<List<Project>> {
        return dao.getProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProjectById(id: String): Project? {
        return dao.getProjectById(id)?.toDomain()
    }

    override suspend fun insertProject(project: Project) {
        dao.insertProject(project.toEntity())
    }

    override suspend fun updateProject(project: Project) {
        dao.updateProject(project.toEntity())
    }

    override suspend fun deleteProject(id: String) {
        dao.deleteProject(id)
    }

    override suspend fun toggleFavorite(id: String) {
        dao.toggleFavorite(id)
    }
}
