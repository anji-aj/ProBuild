package com.example.probuild.features.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.features.reports.domain.ReportsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val repository: ReportsRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = repository.getProjectReportData()
        .map { data ->
            if (data.activeProject == null && data.totalProjectsCount == 0) {
                ReportsUiState.Empty("No projects found. Create a project to start generating reports.")
            } else {
                ReportsUiState.Success(data = data)
            }
        }
        .catch { e ->
            emit(ReportsUiState.Error(e.localizedMessage ?: "Failed to load report data"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReportsUiState.Loading
        )

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            repository.selectActiveProject(projectId)
        }
    }
}
