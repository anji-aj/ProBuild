package com.example.probuild.features.reports.presentation

import com.example.probuild.features.reports.domain.model.ProjectReportData

sealed interface ReportsUiState {
    data object Loading : ReportsUiState
    data class Success(
        val data: ProjectReportData,
        val isExporting: Boolean = false
    ) : ReportsUiState
    data class Empty(
        val message: String = "No active project or reportable data found."
    ) : ReportsUiState
    data class Error(
        val message: String
    ) : ReportsUiState
}
