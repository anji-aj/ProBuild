package com.example.probuild.features.engineering.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.Project
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EngineeringWorkspaceViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _projects = projectsRepository.getProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProjectId = preferenceManager.selectedProjectId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _favorites = engineeringRepository.getFavoriteCalculators()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EngineeringWorkspaceUiState> = combine(
        _projects,
        _selectedProjectId,
        _favorites
    ) { projects, selectedId, favorites ->
        // Handle project selection logic: 
        // 1. If no project selected but projects exist, pick the first one.
        // 2. If selected project no longer exists, pick the first one.
        val selectedProject = projects.find { it.id == selectedId } 
            ?: projects.firstOrNull()

        // Sync with DataStore if the "effective" selection changed
        if (selectedProject?.id != selectedId) {
            viewModelScope.launch {
                preferenceManager.saveSelectedProjectId(selectedProject?.id)
            }
        }

        val history = if (selectedProject != null) {
            engineeringRepository.getCalculationsByProject(selectedProject.id).first()
        } else {
            emptyList()
        }

        EngineeringWorkspaceUiState(
            projects = projects,
            selectedProject = selectedProject,
            history = history,
            favorites = favorites,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EngineeringWorkspaceUiState()
    )

    fun onProjectSelected(projectId: String) {
        viewModelScope.launch {
            preferenceManager.saveSelectedProjectId(projectId)
        }
    }

    fun toggleCalculatorFavorite(type: CalculationType) {
        viewModelScope.launch {
            engineeringRepository.toggleCalculatorFavorite(type)
        }
    }

    fun deleteCalculation(calculationId: String) {
        viewModelScope.launch {
            engineeringRepository.deleteCalculation(calculationId)
        }
    }
}
