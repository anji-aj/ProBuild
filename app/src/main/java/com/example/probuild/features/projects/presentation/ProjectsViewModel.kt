package com.example.probuild.features.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.Project
import com.example.probuild.features.projects.domain.model.ProjectStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val repository: ProjectsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    private val _filterStatus = MutableStateFlow<Set<ProjectStatus>>(emptySet())

    val state: StateFlow<ProjectsUiState> = combine(
        repository.getProjects(),
        _searchQuery,
        _sortOrder,
        _filterStatus
    ) { projects, query, sort, filters ->
        val filtered = projects.filter {
            (query.isBlank() || it.projectName.contains(query, ignoreCase = true) || it.clientName.contains(query, ignoreCase = true)) &&
            (filters.isEmpty() || filters.contains(it.status))
        }.let { list ->
            when (sort) {
                SortOrder.NEWEST -> list.sortedByDescending { it.createdAt }
                SortOrder.OLDEST -> list.sortedBy { it.createdAt }
                SortOrder.BUDGET_HIGH_LOW -> list.sortedByDescending { it.budget.toDoubleOrNull() ?: 0.0 }
                SortOrder.ALPHABETICAL -> list.sortedBy { it.projectName }
                SortOrder.PROGRESS_HIGH_LOW -> list.sortedByDescending { it.progress }
            }
        }
        
        ProjectsUiState(
            projects = filtered,
            isLoading = false,
            searchQuery = query,
            sortOrder = sort,
            filterStatus = filters
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProjectsUiState(isLoading = true))

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleFilterStatus(status: ProjectStatus) {
        val current = _filterStatus.value
        _filterStatus.value = if (current.contains(status)) {
            current - status
        } else {
            current + status
        }
    }

    fun toggleFavorite(projectId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(projectId)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }
}
