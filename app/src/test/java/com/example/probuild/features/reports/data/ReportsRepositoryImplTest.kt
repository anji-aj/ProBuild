package com.example.probuild.features.reports.data

import com.example.probuild.data.local.CalculationDao
import com.example.probuild.data.local.DiaryEntryDao
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.data.local.ProjectDao
import com.example.probuild.data.local.ProjectEntity
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsRepositoryImplTest {

    private val projectDao = mockk<ProjectDao>(relaxed = true)
    private val calculationDao = mockk<CalculationDao>(relaxed = true)
    private val diaryEntryDao = mockk<DiaryEntryDao>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)

    private lateinit var repository: ReportsRepositoryImpl

    private val testProjectEntity = ProjectEntity(
        id = "p1",
        projectName = "Bridge Site A",
        clientName = "City Council",
        location = "Sector 5",
        description = "Bridge construction",
        projectType = "INFRASTRUCTURE",
        priority = "HIGH",
        projectStage = "EXECUTION",
        budget = "500000",
        startDate = "2025-01-01",
        expectedEndDate = "2025-12-31",
        status = "IN_PROGRESS",
        progress = 0.45f,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isFavorite = false
    )

    @Before
    fun setup() {
        repository = ReportsRepositoryImpl(
            projectDao,
            calculationDao,
            diaryEntryDao,
            preferenceManager
        )
    }

    @Test
    fun `test getProjectReportData with no projects returns empty report`() = runTest {
        every { projectDao.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)

        val reportData = repository.getProjectReportData().first()

        assertNull(reportData.activeProject)
        assertEquals(0, reportData.totalProjectsCount)
        assertEquals(0, reportData.allProjects.size)
    }

    @Test
    fun `test getProjectReportData with active project returns aggregated totals`() = runTest {
        every { projectDao.getProjects() } returns flowOf(listOf(testProjectEntity))
        every { preferenceManager.selectedProjectId } returns flowOf("p1")
        every { calculationDao.getCalculationsByProject("p1") } returns flowOf(emptyList())
        every { diaryEntryDao.observeEntriesByProject("p1") } returns flowOf(emptyList())

        val reportData = repository.getProjectReportData().first()

        assertNotNull(reportData.activeProject)
        assertEquals("p1", reportData.activeProject?.id)
        assertEquals("Bridge Site A", reportData.activeProject?.projectName)
        assertEquals(1, reportData.totalProjectsCount)
        assertEquals(0, reportData.totalCalculationsCount)
        assertEquals(0, reportData.totalDiaryEntriesCount)
    }

    @Test
    fun `test selectActiveProject saves id in preference manager`() = runTest {
        repository.selectActiveProject("p1")

        coVerify { preferenceManager.saveSelectedProjectId("p1") }
    }
}
