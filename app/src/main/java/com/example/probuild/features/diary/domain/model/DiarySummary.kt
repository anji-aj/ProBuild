package com.example.probuild.features.diary.domain.model

data class DiarySummary(
    val hasTodayEntry: Boolean = false,
    val todayLabourCount: Int = 0,
    val totalEntryCount: Int = 0,
    val lastEntryTimestamp: Long? = null,
    val entriesThisWeek: Int = 0,
    val totalLabourRecorded: Int = 0,
    val daysWithEntries: Int = 0
)
