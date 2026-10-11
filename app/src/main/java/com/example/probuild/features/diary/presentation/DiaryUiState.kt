package com.example.probuild.features.diary.presentation

import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.diary.domain.model.DiaryPhoto
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.features.projects.domain.model.Project

data class DiaryUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedDateFilter: DateFilter = DateFilter.ALL,
    val customDateRange: Pair<Long?, Long?> = null to null,
    val sortOrder: DiarySortOrder = DateSortOrder.NEWEST_FIRST,
    val summary: DiarySummary = DiarySummary(),
    val isLoading: Boolean = false,
    val selectedProject: Project? = null,
    val isCalendarVisible: Boolean = false,
    
    // Form state (for creating/editing)
    val entryId: String? = null,
    val date: Long = System.currentTimeMillis(),
    val workCompleted: String = "",
    val labourCount: String = "0",
    val materialsUsed: String = "",
    val equipmentUsed: String = "",
    val weather: String = "",
    val issues: String = "",
    val notes: String = "",
    val photos: List<DiaryPhoto> = emptyList(),
    
    // Validation errors
    val workCompletedError: String? = null,
    val dateError: String? = null,
    val labourCountError: String? = null,
    val photoError: String? = null,
    
    val isEntrySaved: Boolean = false,
    val isEntryDeleted: Boolean = false
)

enum class DateFilter(val displayName: String) {
    ALL("All"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom")
}

sealed interface DiarySortOrder {
    val displayName: String
}

enum class DateSortOrder(override val displayName: String) : DiarySortOrder {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First")
}
