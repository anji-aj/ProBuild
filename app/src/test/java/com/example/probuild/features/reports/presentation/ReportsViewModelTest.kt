package com.example.probuild.features.reports.presentation

import com.example.probuild.features.projects.domain.model.Priority
import com.example.probuild.features.projects.domain.model.Project
import com.example.probuild.features.projects.domain.model.ProjectStage
import com.example.probuild.features.projects.domain.model.ProjectStatus
import com.example.probuild.features.projects.domain.model.ProjectType
import com.example.probuild.features.reports.domain.ReportsRepository
import com.example.probuild.features.reports.domain.model.ProjectReportData
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private lateinit var viewModel: ReportsViewModel
    private val reportsRepository = mockk<ReportsRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val testProject = Project(
        id = "p1",
        projectName = "Report Test Project",
        clientName = "ACME Corp",
        location = "Site 1",
        description = "Test project for reporting",
        projectType = ProjectType.COMMERCIAL,
        priority = Priority.HIGH,
        projectStage = ProjectStage.STRUCTURE,
        budget = "100000",
        startDate = "2025-01-01",
        expectedEndDate = "2025-12-31",
        status = ProjectStatus.ACTIVE,
        progress = 0.5f,
        createdAt = 1000L,
        updatedAt = 2000L,
        isFavorite = true
    )

    private val testReportData = ProjectReportData(
        activeProject = testProject,
        totalProjectsCount = 1,
        allProjects = listOf(testProject),
        totalCalculationsCount = 5,
        totalDiaryEntriesCount = 3,
        totalLabourRecorded = 15
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { reportsRepository.getProjectReportData() } returns flowOf(testReportData)
        viewModel = ReportsViewModel(reportsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test report data emission success`() = runTest {
        val state = viewModel.uiState
            .filterIsInstance<ReportsUiState.Success>()
            .first()

        assertEquals("Report Test Project", state.data.activeProject?.projectName)
        assertEquals(5, state.data.totalCalculationsCount)
        assertEquals(3, state.data.totalDiaryEntriesCount)
        assertEquals(15, state.data.totalLabourRecorded)
    }

    @Test
    fun `test report data empty state`() = runTest {
        every { reportsRepository.getProjectReportData() } returns flowOf(ProjectReportData())
        val emptyViewModel = ReportsViewModel(reportsRepository)

        val state = emptyViewModel.uiState
            .filterIsInstance<ReportsUiState.Empty>()
            .first()

        assertTrue(state.message.contains("No projects found"))
    }

    @Test
    fun `test select project triggers repository`() = runTest {
        viewModel.selectProject("p2")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { reportsRepository.selectActiveProject("p2") }
    }
}
