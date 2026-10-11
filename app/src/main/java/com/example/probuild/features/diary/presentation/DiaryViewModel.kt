package com.example.probuild.features.diary.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.diary.domain.DiaryPhotoRepository
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModel(
    private val diaryRepository: SiteDiaryRepository,
    private val photoRepository: DiaryPhotoRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dateFilter = MutableStateFlow(DateFilter.ALL)
    private val _customRange = MutableStateFlow<Pair<Long?, Long?>>(null to null)
    private val _sortOrder = MutableStateFlow<DiarySortOrder>(DateSortOrder.NEWEST_FIRST)

    init {
        loadProjectContext()
        observeEntries()
        observeSummary()
    }

    private fun loadProjectContext() {
        viewModelScope.launch {
            combine(
                projectsRepository.getProjects(),
                preferenceManager.selectedProjectId
            ) { projects, selectedId ->
                projects.find { it.id == selectedId } ?: projects.firstOrNull()
            }.collect { project ->
                _uiState.update { it.copy(selectedProject = project) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeEntries() {
        viewModelScope.launch {
            _uiState.map { it.selectedProject?.id }
                .distinctUntilChanged()
                .flatMapLatest { projectId ->
                    if (projectId == null) flowOf(emptyList())
                    else {
                        // Combine all triggers: search, date filter, range, sort
                        combine(
                            _searchQuery,
                            _dateFilter,
                            _customRange,
                            _sortOrder
                        ) { query, filter, range, sort ->
                            // 1. Get raw project entries (or searched ones if more efficient, 
                            // but prompt says use Flow efficiently, and we need to filter by date anyway)
                            val source = if (query.isBlank()) {
                                diaryRepository.observeEntriesByProject(projectId)
                            } else {
                                diaryRepository.searchEntries(projectId, query.trim())
                            }
                            
                            source.map { list ->
                                // 2. Apply Date Range Filtering
                                val filtered = applyDateFilter(list, filter, range)
                                
                                // 3. Apply Sorting
                                when (sort) {
                                    DateSortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.date }
                                    DateSortOrder.OLDEST_FIRST -> filtered.sortedBy { it.date }
                                    else -> filtered
                                }
                            }
                        }.flattenMerge()
                    }
                }
                .collect { entries ->
                    _uiState.update { it.copy(entries = entries) }
                }
        }
    }

    private fun observeSummary() {
        viewModelScope.launch {
            _uiState.map { it.selectedProject?.id }
                .distinctUntilChanged()
                .flatMapLatest { projectId ->
                    if (projectId == null) flowOf(DiarySummary())
                    else diaryRepository.observeSummary(projectId)
                }
                .collect { summary ->
                    _uiState.update { it.copy(summary = summary) }
                }
        }
    }

    private fun applyDateFilter(list: List<DiaryEntry>, filter: DateFilter, range: Pair<Long?, Long?>): List<DiaryEntry> {
        return when (filter) {
            DateFilter.ALL -> list
            DateFilter.TODAY -> {
                val today = DateUtils.getTodayRange()
                list.filter { it.date in today.first..today.second }
            }
            DateFilter.YESTERDAY -> {
                val yesterday = DateUtils.getYesterdayRange()
                list.filter { it.date in yesterday.first..yesterday.second }
            }
            DateFilter.THIS_WEEK -> {
                val week = DateUtils.getThisWeekRange()
                list.filter { it.date in week.first..week.second }
            }
            DateFilter.THIS_MONTH -> {
                val month = DateUtils.getThisMonthRange()
                list.filter { it.date in month.first..month.second }
            }
            DateFilter.CUSTOM -> {
                val start = range.first ?: 0L
                val end = range.second ?: Long.MAX_VALUE
                list.filter { it.date in start..end }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDateFilterSelected(filter: DateFilter) {
        _dateFilter.value = filter
        _uiState.update { it.copy(selectedDateFilter = filter) }
    }

    fun onCustomDateRangeSelected(start: Long?, end: Long?) {
        val range = (start?.let { DateUtils.getStartOfDay(it) }) to (end?.let { DateUtils.getEndOfDay(it) })
        _customRange.value = range
        _uiState.update { it.copy(customDateRange = range, selectedDateFilter = DateFilter.CUSTOM) }
    }

    fun onSortOrderSelected(order: DiarySortOrder) {
        _sortOrder.value = order
        _uiState.update { it.copy(sortOrder = order) }
    }

    // Form interactions
    fun loadEntry(id: String?) {
        if (id == null) {
            _uiState.update { it.copy(
                entryId = null,
                date = System.currentTimeMillis(),
                workCompleted = "",
                labourCount = "0",
                materialsUsed = "",
                equipmentUsed = "",
                weather = "",
                issues = "",
                notes = "",
                photos = emptyList(),
                workCompletedError = null,
                labourCountError = null,
                isEntrySaved = false,
                isEntryDeleted = false
            ) }
            return
        }
        viewModelScope.launch {
            diaryRepository.getEntryById(id)?.let { entry ->
                _uiState.update { state ->
                    state.copy(
                        entryId = entry.id,
                        date = entry.date,
                        workCompleted = entry.workCompleted,
                        labourCount = entry.labourCount.toString(),
                        materialsUsed = entry.materialsUsed,
                        equipmentUsed = entry.equipmentUsed,
                        weather = entry.weather,
                        issues = entry.issues,
                        notes = entry.notes,
                        isEntrySaved = false,
                        isEntryDeleted = false
                    )
                }
                observePhotos(entry.id)
            }
        }
    }

    private fun observePhotos(entryId: String) {
        viewModelScope.launch {
            photoRepository.observePhotosByDiaryEntry(entryId).collect { photos ->
                _uiState.update { it.copy(photos = photos) }
            }
        }
    }

    fun onPhotoAdded(uri: Uri) {
        val entryId = _uiState.value.entryId ?: return
        val project = _uiState.value.selectedProject ?: return
        viewModelScope.launch {
            photoRepository.addPhoto(entryId, project.id, uri).onFailure {
                _uiState.update { it.copy(photoError = "Failed to attach photo") }
            }
        }
    }

    fun clearPhotoError() {
        _uiState.update { it.copy(photoError = null) }
    }

    /**
     * For NEW entries, we need to generate an ID immediately so photos can be attached.
     */
    fun ensureEntryId() {
        if (_uiState.value.entryId == null) {
            val newId = UUID.randomUUID().toString()
            _uiState.update { it.copy(entryId = newId) }
            observePhotos(newId)
        }
    }

    fun updatePhotoCaption(photoId: String, caption: String) {
        viewModelScope.launch {
            photoRepository.updateCaption(photoId, caption)
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            photoRepository.deletePhoto(photoId)
        }
    }

    fun duplicateEntry(id: String) {
        viewModelScope.launch {
            diaryRepository.getEntryById(id)?.let { entry ->
                _uiState.update { state ->
                    state.copy(
                        entryId = null, // New entry
                        date = System.currentTimeMillis(), // Today
                        workCompleted = entry.workCompleted,
                        labourCount = entry.labourCount.toString(),
                        materialsUsed = entry.materialsUsed,
                        equipmentUsed = entry.equipmentUsed,
                        weather = entry.weather,
                        issues = entry.issues,
                        notes = entry.notes,
                        isEntrySaved = false
                    )
                }
            }
        }
    }

    fun openTodaysEntry() {
        val project = _uiState.value.selectedProject ?: return
        viewModelScope.launch {
            val todayRange = DateUtils.getTodayRange()
            val entries = _uiState.value.entries.filter { it.date in todayRange.first..todayRange.second }
            val existing = entries.maxByOrNull { it.updatedAt }
            
            if (existing != null) {
                loadEntry(existing.id)
            } else {
                loadEntry(null)
            }
        }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date) }
        validateForm()
    }

    fun onWorkCompletedChange(value: String) {
        _uiState.update { it.copy(workCompleted = value) }
        validateForm()
    }

    fun onLabourCountChange(value: String) {
        _uiState.update { it.copy(labourCount = value) }
        validateForm()
    }

    fun onMaterialsUsedChange(value: String) {
        _uiState.update { it.copy(materialsUsed = value) }
    }

    fun onEquipmentUsedChange(value: String) {
        _uiState.update { it.copy(equipmentUsed = value) }
    }

    fun onWeatherChange(value: String) {
        _uiState.update { it.copy(weather = value) }
    }

    fun onIssuesChange(value: String) {
        _uiState.update { it.copy(issues = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    private fun validateForm() {
        val s = _uiState.value
        val labour = s.labourCount.toIntOrNull()
        
        _uiState.update {
            it.copy(
                workCompletedError = if (s.workCompleted.isBlank()) "Required" else null,
                labourCountError = if (labour == null || labour < 0) "Must be 0 or greater" else null
            )
        }
    }

    fun saveEntry() {
        validateForm()
        val s = _uiState.value
        val project = s.selectedProject ?: return
        val labour = s.labourCount.toIntOrNull() ?: 0
        
        if (s.workCompleted.isBlank() || s.workCompletedError != null || s.labourCountError != null) return

        viewModelScope.launch {
            val entry = DiaryEntry(
                id = s.entryId ?: UUID.randomUUID().toString(),
                projectId = project.id,
                date = s.date,
                workCompleted = s.workCompleted,
                labourCount = labour,
                materialsUsed = s.materialsUsed,
                equipmentUsed = s.equipmentUsed,
                weather = s.weather,
                issues = s.issues,
                notes = s.notes,
                createdAt = if (s.entryId == null) System.currentTimeMillis() else 0, // Handled by Room or Repo if needed, but let's be explicit
                updatedAt = System.currentTimeMillis()
            )
            
            if (s.entryId == null) {
                diaryRepository.insertEntry(entry)
            } else {
                diaryRepository.updateEntry(entry)
            }
            _uiState.update { it.copy(isEntrySaved = true) }
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            photoRepository.deletePhotosByDiaryEntry(id)
            diaryRepository.deleteEntry(id)
            _uiState.update { it.copy(isEntryDeleted = true) }
        }
    }
}
