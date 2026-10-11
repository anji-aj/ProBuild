package com.example.probuild.features.engineering.presentation.beam

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
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
class BeamCalculatorViewModelTest {

    private lateinit var viewModel: BeamCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = BeamCalculatorViewModel(
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
    fun `test live updates and validation`() = runTest {
        viewModel.onLengthChange("5")
        viewModel.onWidthChange("30")
        viewModel.onDepthChange("50")
        viewModel.onMainBarCountChange("4")
        viewModel.onMainBarDiameterChange("16")
        viewModel.onStirrupDiameterChange("8")
        viewModel.onStirrupSpacingChange("200")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(0.75, state.concreteVolumeM3 ?: 0.0, 0.0001)
        assertEquals(26, state.stirrupCount)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test validation error states`() = runTest {
        // Zero length
        viewModel.onLengthChange("0")
        assertEquals("Must be > 0", viewModel.uiState.value.lengthError)
        
        // Negative count
        viewModel.onMainBarCountChange("-2")
        assertEquals("Must be > 0", viewModel.uiState.value.mainBarCountError)
        
        // Invalid spacing
        viewModel.onStirrupSpacingChange("abc")
        assertEquals("Invalid number", viewModel.uiState.value.stirrupSpacingError)
        
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test history restoration`() = runTest {
        val calcId = "beam1"
        val inputs = listOf(
            CalculationInput(key = "length", value = 6.0, unit = "m"),
            CalculationInput(key = "width", value = 25.0, unit = "cm"),
            CalculationInput(key = "depth", value = 40.0, unit = "cm"),
            CalculationInput(key = "mainBarCount", value = 6.0, unit = "pcs"),
            CalculationInput(key = "mainBarDiameter", value = 20.0, unit = "mm"),
            CalculationInput(key = "stirrupDiameter", value = 10.0, unit = "mm"),
            CalculationInput(key = "stirrupSpacing", value = 150.0, unit = "mm")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.BEAM,
            displayName = "Beam Quantities",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 50.0,
            resultUnit = "kg",
            timestamp = 4000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("6.0", state.length)
        assertEquals(DimensionUnit.METER, state.lengthUnit)
        assertEquals("25.0", state.width)
        assertEquals(DimensionUnit.CENTIMETER, state.widthUnit)
        assertEquals("6", state.mainBarCount)
        assertEquals(DimensionUnit.MILLIMETER, state.mainBarDiameterUnit)
        assertTrue(state.isCalculateEnabled)
    }
}
