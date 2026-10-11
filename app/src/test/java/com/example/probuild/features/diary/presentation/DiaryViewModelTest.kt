package com.example.probuild.features.diary.presentation

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.diary.domain.DiaryPhotoRepository
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.projects.domain.ProjectsRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    private lateinit var viewModel: DiaryViewModel
    private val diaryRepository = mockk<SiteDiaryRepository>(relaxed = true)
    private val photoRepository = mockk<DiaryPhotoRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
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
    fun `initial state - starts with default values`() {
        val state = viewModel.uiState.value
        assertTrue(state.entries.isEmpty())
        assertEquals("", state.searchQuery)
        assertNull(state.selectedProject)
    }

    @Test
    fun `validation - work completed is required`() = runTest {
        viewModel.onWorkCompletedChange("")
        advanceUntilIdle()
        
        assertEquals("Required", viewModel.uiState.value.workCompletedError)
        
        viewModel.onWorkCompletedChange("Done some work")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.workCompletedError)
    }

    @Test
    fun `validation - labour count must be non-negative`() = runTest {
        viewModel.onLabourCountChange("-1")
        advanceUntilIdle()
        assertEquals("Must be 0 or greater", viewModel.uiState.value.labourCountError)
        
        viewModel.onLabourCountChange("5")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.labourCountError)
    }

    @Test
    fun `search - updates query in state`() {
        viewModel.onSearchQueryChange("Concrete")
        assertEquals("Concrete", viewModel.uiState.value.searchQuery)
    }
}
