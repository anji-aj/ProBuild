package com.example.probuild.features.dashboard.presentation

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.dashboard.domain.DashboardRepository
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.features.notifications.domain.NotificationRepository
import com.example.probuild.features.projects.domain.ProjectsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private val dashboardRepository = mockk<DashboardRepository>(relaxed = true)
    private val notificationRepository = mockk<NotificationRepository>(relaxed = true)
    private val diaryRepository = mockk<SiteDiaryRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val projectId = "p1"
    private val testSummary = DiarySummary(
        hasTodayEntry = true,
        todayLabourCount = 10,
        totalEntryCount = 50
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { dashboardRepository.getRecentProjects() } returns flowOf(emptyList())
        every { notificationRepository.getUnreadCount() } returns flowOf(0)
        every { preferenceManager.selectedProjectId } returns flowOf(projectId)
        every { diaryRepository.observeSummary(projectId) } returns flowOf(testSummary)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())

        viewModel = DashboardViewModel(
            dashboardRepository,
            notificationRepository,
            diaryRepository,
            preferenceManager,
            projectsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test diary summary observation`() = runTest {
        val state = viewModel.uiState
            .filterIsInstance<DashboardUiState.Success>()
            .filter { it.diarySummary == testSummary }
            .first()
        assertEquals(testSummary, state.diarySummary)
    }
}
