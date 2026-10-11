package com.example.probuild.features.projects.data.mapper

import com.example.probuild.data.local.ProjectEntity
import com.example.probuild.features.projects.domain.model.*

fun ProjectEntity.toDomain(): Project {
    return Project(
        id = id,
        projectName = projectName,
        clientName = clientName,
        location = location,
        description = description,
        projectType = safeEnumValueOf<ProjectType>(projectType, ProjectType.OTHER),
        priority = safeEnumValueOf<Priority>(priority, Priority.MEDIUM),
        projectStage = safeEnumValueOf<ProjectStage>(projectStage, ProjectStage.PLANNING),
        budget = budget,
        startDate = startDate,
        expectedEndDate = expectedEndDate,
        status = safeEnumValueOf<ProjectStatus>(status, ProjectStatus.PLANNING),
        progress = progress,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite
    )
}

inline fun <reified T : Enum<T>> safeEnumValueOf(name: String, fallback: T): T {
    return try {
        enumValueOf<T>(name)
    } catch (e: Exception) {
        fallback
    }
}

fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        id = id,
        projectName = projectName,
        clientName = clientName,
        location = location,
        description = description,
        projectType = projectType.name,
        priority = priority.name,
        projectStage = projectStage.name,
        budget = budget,
        startDate = startDate,
        expectedEndDate = expectedEndDate,
        status = status.name,
        progress = progress,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite
    )
}
