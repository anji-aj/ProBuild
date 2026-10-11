package com.example.probuild.features.reports.domain

import com.example.probuild.features.reports.domain.model.ProjectReportData
import kotlinx.coroutines.flow.Flow

interface ReportsRepository {
    fun getProjectReportData(): Flow<ProjectReportData>
    suspend fun selectActiveProject(projectId: String)
}
