package com.example.probuild.features.diary.domain

import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.diary.domain.model.DiarySummary
import kotlinx.coroutines.flow.Flow

interface SiteDiaryRepository {
    fun observeEntriesByProject(projectId: String): Flow<List<DiaryEntry>>
    suspend fun getEntryById(id: String): DiaryEntry?
    suspend fun insertEntry(entry: DiaryEntry)
    suspend fun updateEntry(entry: DiaryEntry)
    suspend fun deleteEntry(id: String)
    fun searchEntries(projectId: String, query: String): Flow<List<DiaryEntry>>
    fun observeSummary(projectId: String): Flow<DiarySummary>
}
