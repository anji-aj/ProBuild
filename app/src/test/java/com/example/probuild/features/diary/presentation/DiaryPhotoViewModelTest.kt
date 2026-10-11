package com.example.probuild.features.diary.presentation

import android.net.Uri
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.diary.domain.DiaryPhotoRepository
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.diary.domain.model.DiaryPhoto
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryPhotoViewModelTest {

    private lateinit var viewModel: DiaryViewModel
    private val diaryRepository = mockk<SiteDiaryRepository>(relaxed = true)
    private val photoRepository = mockk<DiaryPhotoRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val testProject = Project(
        id = "p1", projectName = "Site Alpha", clientName = "", location = "", 
        description = "", projectType = ProjectType.COMMERCIAL, priority = Priority.HIGH, 
        projectStage = ProjectStage.FOUNDATION, budget = "", startDate = "", expectedEndDate = "", 
        status = ProjectStatus.ACTIVE, progress = 0.5f, updatedAt = 0L
    )

    private val samplePhoto = DiaryPhoto(
        id = "photo1",
        diaryEntryId = "entry1",
        projectId = "p1",
        filePath = "/storage/photo1.jpg",
        caption = "Foundational slab poured",
        createdAt = 1000L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(listOf(testProject))
        every { preferenceManager.selectedProjectId } returns flowOf("p1")
        
        viewModel = DiaryViewModel(
            diaryRepository,
            photoRepository,
            projectsRepository,
            preferenceManager
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadEntry - loads photos for specified entry id into uiState`() = runTest {
        coEvery { diaryRepository.getEntryById("entry1") } returns DiaryEntry(
            id = "entry1", projectId = "p1", date = 1000L, workCompleted = "Pouring slab",
            labourCount = 4, materialsUsed = "", equipmentUsed = "", weather = "", issues = "", notes = "",
            createdAt = 1000L, updatedAt = 1000L
        )
        every { photoRepository.observePhotosByDiaryEntry("entry1") } returns flowOf(listOf(samplePhoto))

        viewModel.loadEntry("entry1")
        advanceUntilIdle()

        val photos = viewModel.uiState.value.photos
        assertEquals(1, photos.size)
        assertEquals("photo1", photos[0].id)
        assertEquals("Foundational slab poured", photos[0].caption)
    }

    @Test
    fun `empty state - photo list is empty when new entry loaded or entry has no photos`() = runTest {
        coEvery { diaryRepository.getEntryById("entry2") } returns DiaryEntry(
            id = "entry2", projectId = "p1", date = 1000L, workCompleted = "Site prep",
            labourCount = 2, materialsUsed = "", equipmentUsed = "", weather = "", issues = "", notes = "",
            createdAt = 1000L, updatedAt = 1000L
        )
        every { photoRepository.observePhotosByDiaryEntry("entry2") } returns flowOf(emptyList())

        viewModel.loadEntry("entry2")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.photos.isEmpty())
    }

    @Test
    fun `add photo - triggers repository addPhoto and updates state`() = runTest {
        viewModel.ensureEntryId()
        val entryId = viewModel.uiState.value.entryId
        assertNotNull(entryId)

        val mockUri = mockk<Uri>()
        coEvery { photoRepository.addPhoto(entryId!!, "p1", mockUri) } returns Result.success(samplePhoto)

        viewModel.onPhotoAdded(mockUri)
        advanceUntilIdle()

        coVerify { photoRepository.addPhoto(entryId!!, "p1", mockUri) }
    }

    @Test
    fun `add photo error state - failure updates photoError in uiState`() = runTest {
        viewModel.ensureEntryId()
        val entryId = viewModel.uiState.value.entryId!!

        val mockUri = mockk<Uri>()
        coEvery { photoRepository.addPhoto(entryId, "p1", mockUri) } returns Result.failure(Exception("Disk full"))

        viewModel.onPhotoAdded(mockUri)
        advanceUntilIdle()

        assertEquals("Failed to attach photo", viewModel.uiState.value.photoError)

        viewModel.clearPhotoError()
        assertNull(viewModel.uiState.value.photoError)
    }

    @Test
    fun `update caption - delegates to repository`() = runTest {
        viewModel.updatePhotoCaption("photo1", "New caption text")
        advanceUntilIdle()

        coVerify { photoRepository.updateCaption("photo1", "New caption text") }
    }

    @Test
    fun `delete photo - delegates to repository`() = runTest {
        viewModel.deletePhoto("photo1")
        advanceUntilIdle()

        coVerify { photoRepository.deletePhoto("photo1") }
    }
}
