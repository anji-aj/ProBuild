package com.example.probuild.features.diary.data

import com.example.probuild.data.local.DiaryEntryDao
import com.example.probuild.data.local.DiaryEntryEntity
import com.example.probuild.data.local.DiaryPhotoDao
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class SiteDiaryRepositoryImpl(
    private val diaryEntryDao: DiaryEntryDao,
    private val diaryPhotoDao: DiaryPhotoDao? = null
) : SiteDiaryRepository {

    override fun observeEntriesByProject(projectId: String): Flow<List<DiaryEntry>> {
        return diaryEntryDao.observeEntriesByProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: String): DiaryEntry? {
        return diaryEntryDao.getEntryById(id)?.toDomain()
    }

    override suspend fun insertEntry(entry: DiaryEntry) {
        diaryEntryDao.insertEntry(DiaryEntryEntity.fromDomain(entry))
    }

    override suspend fun updateEntry(entry: DiaryEntry) {
        diaryEntryDao.updateEntry(DiaryEntryEntity.fromDomain(entry))
    }

    override suspend fun deleteEntry(id: String) {
        diaryPhotoDao?.let { photoDao ->
            try {
                val photos = photoDao.getPhotosByDiaryEntrySync(id)
                photos.forEach { entity ->
                    try {
                        val file = File(entity.filePath)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        // Handle missing/already deleted files gracefully
                    }
                }
                photoDao.deletePhotosByDiaryEntry(id)
            } catch (e: Exception) {
                // Graceful error handling
            }
        }
        diaryEntryDao.deleteEntry(id)
    }

    override fun searchEntries(projectId: String, query: String): Flow<List<DiaryEntry>> {
        return diaryEntryDao.searchEntries(projectId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeSummary(projectId: String): Flow<DiarySummary> {
        val todayRange = DateUtils.getTodayRange()
        val weekRange = DateUtils.getThisWeekRange()
        
        return diaryEntryDao.observeEntriesByProject(projectId).map { entities ->
            val entries = entities.map { it.toDomain() }
            
            val todayEntries = entries.filter { it.date in todayRange.first..todayRange.second }
            val weekEntries = entries.filter { it.date in weekRange.first..weekRange.second }
            
            DiarySummary(
                hasTodayEntry = todayEntries.isNotEmpty(),
                todayLabourCount = todayEntries.sumOf { it.labourCount },
                totalEntryCount = entries.size,
                lastEntryTimestamp = entries.firstOrNull()?.date,
                entriesThisWeek = weekEntries.size,
                totalLabourRecorded = entries.sumOf { it.labourCount },
                daysWithEntries = entries.distinctBy { DateUtils.getStartOfDay(it.date) }.size
            )
        }
    }
}
