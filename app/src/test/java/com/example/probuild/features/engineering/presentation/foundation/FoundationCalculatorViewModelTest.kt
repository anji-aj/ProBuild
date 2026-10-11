package com.example.probuild.features.engineering.presentation.foundation

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.FoundationCalculator
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationInput
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.ProjectsRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class FoundationCalculatorViewModelTest {

    private lateinit var viewModel: FoundationCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = FoundationCalculatorViewModel(
            engineeringRepository,
            projectsRepository,
            preferenceManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test square mode links length and width`() = runTest {
        viewModel.onTypeSelected(FoundationCalculator.FoundationType.SQUARE)
        viewModel.onLengthChange("1.5")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("1.5", state.length)
        assertEquals("1.5", state.width)
        
        // Manual width change should be ignored in square mode
        viewModel.onWidthChange("2.0")
        assertEquals("1.5", viewModel.uiState.value.width)
    }

    @Test
    fun `test validation error states`() = runTest {
        viewModel.onTypeSelected(FoundationCalculator.FoundationType.RECTANGULAR)
        
        // Zero thickness
        viewModel.onThicknessChange("0")
        assertEquals("Must be > 0", viewModel.uiState.value.thicknessError)
        
        // Invalid spacing
        viewModel.onMainSpacingChange("-5")
        assertEquals("Must be > 0", viewModel.uiState.value.mainSpacingError)
        
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test repeat functionality restores type and inputs`() = runTest {
        val calcId = "found1"
        val inputs = listOf(
            CalculationInput(key = "type", value = 0.0, unit = "SQUARE"),
            CalculationInput(key = "length", value = 1.2, unit = "m"),
            CalculationInput(key = "width", value = 1.2, unit = "m"),
            CalculationInput(key = "thickness", value = 30.0, unit = "cm"),
            CalculationInput(key = "mainDiameter", value = 12.0, unit = "mm"),
            CalculationInput(key = "mainSpacing", value = 15.0, unit = "cm"),
            CalculationInput(key = "distDiameter", value = 12.0, unit = "mm"),
            CalculationInput(key = "distSpacing", value = 15.0, unit = "cm")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.FOUNDATION,
            displayName = "Foundation takeoff",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 25.0,
            resultUnit = "kg",
            timestamp = 7000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(FoundationCalculator.FoundationType.SQUARE, state.selectedType)
        assertEquals("1.2", state.length)
        assertEquals("1.2", state.width)
        assertEquals(DimensionUnit.CENTIMETER, state.thicknessUnit)
        assertTrue(state.isCalculateEnabled)
    }
}
