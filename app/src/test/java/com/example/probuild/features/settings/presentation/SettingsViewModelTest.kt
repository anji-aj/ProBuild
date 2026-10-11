package com.example.probuild.features.settings.presentation

import com.example.probuild.features.settings.domain.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.getActiveProjectName() } returns flowOf("Test Project")
        every { settingsRepository.getActiveProjectId() } returns flowOf("p1")
        every { settingsRepository.getUnitSystem() } returns flowOf("Metric")
        every { settingsRepository.getNotificationsEnabled() } returns flowOf(true)
        every { settingsRepository.getDailyReminderEnabled() } returns flowOf(true)
        every { settingsRepository.getThemeMode() } returns flowOf("SYSTEM")

        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial uiState emission`() = runTest {
        val state = viewModel.uiState.first { it.activeProjectName != null }

        assertEquals("Test Project", state.activeProjectName)
        assertEquals("p1", state.activeProjectId)
        assertEquals("Metric", state.unitSystem)
        assertEquals(true, state.notificationsEnabled)
        assertEquals(true, state.dailyReminderEnabled)
        assertEquals("SYSTEM", state.themeMode)
    }

    @Test
    fun `test set unit system triggers repository`() = runTest {
        viewModel.setUnitSystem("Imperial")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setUnitSystem("Imperial") }
    }

    @Test
    fun `test set notifications enabled triggers repository`() = runTest {
        viewModel.setNotificationsEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setNotificationsEnabled(false) }
    }

    @Test
    fun `test set theme mode triggers repository`() = runTest {
        viewModel.setThemeMode("DARK")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setThemeMode("DARK") }
    }

    @Test
    fun `test clear active project calculations triggers repository`() = runTest {
        viewModel.clearActiveProjectCalculations()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.clearActiveProjectCalculationHistory() }
    }

    @Test
    fun `test reset preferences triggers repository`() = runTest {
        viewModel.resetPreferences()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.resetAllPreferences() }
    }
}
