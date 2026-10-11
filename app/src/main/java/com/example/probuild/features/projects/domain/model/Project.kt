package com.example.probuild.features.projects.domain.model

/**
 * Unified Domain Model for ProBuild Projects.
 */
data class Project(
    val id: String,
    val projectName: String,
    val clientName: String,
    val location: String,
    val description: String = "",
    val projectType: ProjectType = ProjectType.OTHER,
    val priority: Priority = Priority.MEDIUM,
    val projectStage: ProjectStage = ProjectStage.PLANNING,
    val budget: String = "",
    val startDate: String = "",
    val expectedEndDate: String = "",
    val status: ProjectStatus = ProjectStatus.PLANNING,
    val progress: Float = 0f, // 0.0 to 1.0
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

enum class ProjectStatus {
    PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
}

enum class ProjectType {
    RESIDENTIAL, COMMERCIAL, ROAD, BRIDGE, INDUSTRIAL, OTHER
}

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class ProjectStage {
    PLANNING, SURVEY, EXCAVATION, FOUNDATION, STRUCTURE, FINISHING, COMPLETED
}
