package com.example.probuild.features.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class UpsertProjectViewModel(
    private val repository: ProjectsRepository
) : ViewModel() {

    private val _projectName = MutableStateFlow("")
    val projectName = _projectName.asStateFlow()

    private val _clientName = MutableStateFlow("")
    val clientName = _clientName.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _budget = MutableStateFlow("")
    val budget = _budget.asStateFlow()

    private val _projectType = MutableStateFlow(ProjectType.RESIDENTIAL)
    val projectType = _projectType.asStateFlow()

    private val _priority = MutableStateFlow(Priority.MEDIUM)
    val priority = _priority.asStateFlow()

    private val _projectStage = MutableStateFlow(ProjectStage.PLANNING)
    val projectStage = _projectStage.asStateFlow()

    private val _status = MutableStateFlow(ProjectStatus.PLANNING)
    val status = _status.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _startDate = MutableStateFlow("")
    val startDate = _startDate.asStateFlow()

    private val _expectedEndDate = MutableStateFlow("")
    val expectedEndDate = _expectedEndDate.asStateFlow()

    private var editingProjectId: String? = null

    private val _isSaved = MutableSharedFlow<Boolean>()
    val isSaved = _isSaved.asSharedFlow()

    fun loadProject(id: String?) {
        if (id == null) return
        editingProjectId = id
        viewModelScope.launch {
            repository.getProjectById(id)?.let { project ->
                _projectName.value = project.projectName
                _clientName.value = project.clientName
                _location.value = project.location
                _description.value = project.description
                _budget.value = project.budget
                _projectType.value = project.projectType
                _priority.value = project.priority
                _projectStage.value = project.projectStage
                _status.value = project.status
                _progress.value = project.progress
                _startDate.value = project.startDate
                _expectedEndDate.value = project.expectedEndDate
            }
        }
    }

    fun onProjectNameChange(value: String) { _projectName.value = value }
    fun onClientNameChange(value: String) { _clientName.value = value }
    fun onLocationChange(value: String) { _location.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onBudgetChange(value: String) { _budget.value = value }
    fun onProjectTypeChange(value: ProjectType) { _projectType.value = value }
    fun onPriorityChange(value: Priority) { _priority.value = value }
    fun onProjectStageChange(value: ProjectStage) { _projectStage.value = value }
    fun onStatusChange(value: ProjectStatus) { _status.value = value }
    fun onProgressChange(value: Float) { _progress.value = value }
    fun onStartDateChange(value: String) { _startDate.value = value }
    fun onEndDateChange(value: String) { _expectedEndDate.value = value }

    fun saveProject() {
        if (_projectName.value.isBlank()) return

        viewModelScope.launch {
            val project = Project(
                id = editingProjectId ?: UUID.randomUUID().toString(),
                projectName = _projectName.value,
                clientName = _clientName.value,
                location = _location.value,
                description = _description.value,
                projectType = _projectType.value,
                priority = _priority.value,
                projectStage = _projectStage.value,
                budget = _budget.value,
                startDate = _startDate.value,
                expectedEndDate = _expectedEndDate.value,
                status = _status.value,
                progress = _progress.value,
                updatedAt = System.currentTimeMillis()
            )

            if (editingProjectId == null) {
                repository.insertProject(project)
            } else {
                repository.updateProject(project)
            }
            _isSaved.emit(true)
        }
    }
}
