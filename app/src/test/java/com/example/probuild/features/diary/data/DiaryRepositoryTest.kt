package com.example.probuild.features.diary.data

import com.example.probuild.data.local.DiaryEntryDao
import com.example.probuild.data.local.DiaryEntryEntity
import com.example.probuild.features.diary.domain.model.DiaryEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DiaryRepositoryTest {

    private val diaryEntryDao = mockk<DiaryEntryDao>(relaxed = true)
    private val repository = SiteDiaryRepositoryImpl(diaryEntryDao)

    private val testEntry = DiaryEntry(
        id = "1",
        projectId = "p1",
        date = 1000L,
        workCompleted = "Foundation work",
        labourCount = 5,
        materialsUsed = "Concrete",
        equipmentUsed = "Mixer",
        weather = "Sunny",
        issues = "",
        notes = "On schedule",
        createdAt = 1000L,
        updatedAt = 1000L
    )

    @Test
    fun `observeEntriesByProject - maps entities to domain`() = runTest {
        val entities = listOf(DiaryEntryEntity.fromDomain(testEntry))
        coEvery { diaryEntryDao.observeEntriesByProject("p1") } returns flowOf(entities)

        val result = repository.observeEntriesByProject("p1").first()

        assertEquals(1, result.size)
        assertEquals(testEntry, result[0])
    }

    @Test
    fun `insertEntry - calls dao insert`() = runTest {
        repository.insertEntry(testEntry)
        coVerify { diaryEntryDao.insertEntry(any()) }
    }

    @Test
    fun `deleteEntry - calls dao delete`() = runTest {
        repository.deleteEntry("1")
        coVerify { diaryEntryDao.deleteEntry("1") }
    }

    @Test
    fun `searchEntries - calls dao search and maps results`() = runTest {
        val query = "Concrete"
        val entities = listOf(DiaryEntryEntity.fromDomain(testEntry))
        coEvery { diaryEntryDao.searchEntries("p1", query) } returns flowOf(entities)

        val result = repository.searchEntries("p1", query).first()

        assertEquals(1, result.size)
        assertEquals(testEntry, result[0])
    }
}
