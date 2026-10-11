package com.example.probuild.features.dashboard.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.R
import com.example.probuild.di.ServiceLocator
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.dashboard.domain.DashboardAction
import com.example.probuild.features.dashboard.domain.DashboardRepository
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.features.notifications.domain.NotificationRepository
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.ProjectStatus
import com.example.probuild.navigation.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: DashboardRepository,
    private val notificationRepository: NotificationRepository,
    private val diaryRepository: SiteDiaryRepository,
    private val preferenceManager: PreferenceManager,
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DashboardUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _allProjects = projectsRepository.getProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _unreadCount = notificationRepository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _diarySummary = preferenceManager.selectedProjectId
        .flatMapLatest { id ->
            if (id == null) flowOf(DiarySummary())
            else diaryRepository.observeSummary(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiarySummary())

    val uiState: StateFlow<DashboardUiState> = combine(
        _searchQuery,
        _allProjects,
        _unreadCount,
        _diarySummary
    ) { rawQuery, allProjects, count, summary ->
        val query = rawQuery.trim().lowercase()
        val recentProjects = allProjects.sortedByDescending { it.updatedAt }.take(3)
        val filteredProjects = if (query.isBlank()) {
            recentProjects
        } else {
            recentProjects.filter {
                it.projectName.lowercase().contains(query) ||
                it.clientName.lowercase().contains(query) ||
                it.location.lowercase().contains(query) ||
                it.status.name.lowercase().contains(query)
            }
        }

        val filteredActions = if (query.isBlank()) {
            getDashboardActions()
        } else {
            getDashboardActions().filter { action ->
                val keywords = when (action.titleRes) {
                    R.string.action_projects -> listOf("projects", "sites", "construction", "manage", "my projects")
                    R.string.action_engineering -> listOf("calculators", "math", "tools", "formulas", "engineering", "workspace")
                    R.string.action_diary -> listOf("diary", "log", "journal", "daily", "site diary")
                    R.string.action_material -> listOf("material", "inventory", "delivery", "stock", "material tracker")
                    R.string.action_estimator -> listOf("estimator", "cost", "budget", "price", "cost estimator")
                    R.string.action_reports -> listOf("reports", "exports", "documents", "site reports")
                    else -> emptyList()
                }
                keywords.any { it.lowercase().contains(query) }
            }
        }

        val activeCount = allProjects.count { it.status != ProjectStatus.COMPLETED && it.status != ProjectStatus.CANCELLED }
        val completedCount = allProjects.count { it.status == ProjectStatus.COMPLETED }

        DashboardUiState.Success(
            greetingRes = getGreetingRes(),
            actions = filteredActions,
            recentProjects = filteredProjects,
            allProjectsCount = allProjects.size,
            activeProjectsCount = activeCount,
            completedProjectsCount = completedCount,
            searchQuery = rawQuery,
            unreadNotificationCount = count,
            diarySummary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onActionClick(action: DashboardAction) {
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.Navigate(action.screen))
        }
    }

    fun onProjectClick(projectId: String) {
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.Navigate(Screen.ProjectPreview(projectId)))
        }
    }

    fun onNotificationClick() {
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.Navigate(Screen.Notifications))
        }
    }

    fun onProfileClick() {
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.Navigate(Screen.Profile))
        }
    }

    fun loadDashboard() {
        // Data is reactive via flows
    }

    private fun getGreetingRes(): Int {
        return when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
            in 5..11 -> R.string.greeting_morning
            in 12..16 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
    }

    private fun getDashboardActions(): List<DashboardAction> = listOf(
        DashboardAction(R.string.action_projects, R.string.desc_projects, Icons.Default.Engineering, Screen.Projects),
        DashboardAction(R.string.action_engineering, R.string.desc_engineering, Icons.Default.Calculate, Screen.EngineeringWorkspace),
        DashboardAction(R.string.action_drawing, R.string.desc_drawing, Icons.Default.Draw, Screen.DrawingStudio()),
        DashboardAction(R.string.action_diary, R.string.desc_diary, Icons.AutoMirrored.Filled.Assignment, Screen.SiteDiary),
        DashboardAction(R.string.action_material, R.string.desc_material, Icons.Default.Inventory, Screen.MaterialTracker),
        DashboardAction(R.string.action_estimator, R.string.desc_estimator, Icons.Default.RequestQuote, Screen.CostEstimator),
        DashboardAction(R.string.action_reports, R.string.desc_reports, Icons.Default.Construction, Screen.Reports)
    )
}
