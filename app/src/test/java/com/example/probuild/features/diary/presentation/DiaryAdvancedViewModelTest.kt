package com.example.probuild.features.diary.presentation

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.diary.domain.DiaryPhotoRepository
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.*
import com.example.probuild.util.DateUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryAdvancedViewModelTest {

    private lateinit var viewModel: DiaryViewModel
    private val diaryRepository = mockk<SiteDiaryRepository>(relaxed = true)
    private val photoRepository = mockk<DiaryPhotoRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val projectId = "p1"
    private val today = System.currentTimeMillis()
    private val yesterday = today - 86400000L
    private val lastWeek = today - (86400000L * 7)

    private val entries = listOf(
        createEntry("1", today, "Work Today"),
        createEntry("2", yesterday, "Work Yesterday"),
        createEntry("3", lastWeek, "Work Last Week")
    )

    private fun createEntry(id: String, date: Long, work: String) = DiaryEntry(
        id = id, projectId = projectId, date = date, workCompleted = work,
        labourCount = 5, materialsUsed = "", equipmentUsed = "", weather = "", issues = "", notes = "",
        createdAt = date, updatedAt = date
    )

    private val project = Project(
        id = projectId, projectName = "Test Project", clientName = "", location = "", 
        description = "", projectType = ProjectType.RESIDENTIAL, priority = Priority.MEDIUM, 
        projectStage = ProjectStage.PLANNING, budget = "", startDate = "", expectedEndDate = "", 
        status = ProjectStatus.PLANNING, progress = 0f, updatedAt = 0L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(listOf(project))
        every { preferenceManager.selectedProjectId } returns flowOf(projectId)
        every { diaryRepository.observeEntriesByProject(projectId) } returns flowOf(entries)
        every { diaryRepository.searchEntries(projectId, any()) } answers {
            val query = secondArg<String>().lowercase()
            flowOf(entries.filter { it.workCompleted.lowercase().contains(query) })
        }
        every { diaryRepository.observeSummary(projectId) } returns flowOf(DiarySummary())
        
        viewModel = DiaryViewModel(
            diaryRepository,
            photoRepository,
            projectsRepository,
            preferenceManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test date filtering - today`() = runTest {
        viewModel.onDateFilterSelected(DateFilter.TODAY)
        advanceUntilIdle()
        
        val filtered = viewModel.uiState.value.entries
        assertEquals(1, filtered.size)
        assertEquals("1", filtered[0].id)
    }

    @Test
    fun `test combined filtering - search and date`() = runTest {
        viewModel.onSearchQueryChange("Work")
        viewModel.onDateFilterSelected(DateFilter.YESTERDAY)
        advanceUntilIdle()
        
        val filtered = viewModel.uiState.value.entries
        assertEquals(1, filtered.size)
        assertEquals("2", filtered[0].id)
    }

    @Test
    fun `test sorting - oldest first`() = runTest {
        viewModel.onSortOrderSelected(DateSortOrder.OLDEST_FIRST)
        advanceUntilIdle()
        
        val sorted = viewModel.uiState.value.entries
        assertEquals("3", sorted[0].id) // Last week
        assertEquals("1", sorted[2].id) // Today
    }

    @Test
    fun `test duplicate entry - creates new entry state with today date`() = runTest {
        val originalId = "2"
        coEvery { diaryRepository.getEntryById(originalId) } returns entries.find { it.id == originalId }
        
        viewModel.duplicateEntry(originalId)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNull(state.entryId) // New entry
        assertEquals("Work Yesterday", state.workCompleted)
        assertTrue(DateUtils.isSameDay(System.currentTimeMillis(), state.date))
    }
}
