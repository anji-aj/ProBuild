package com.example.probuild.features.engineering.presentation.slab

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
class SlabCalculatorViewModelTest {

    private lateinit var viewModel: SlabCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = SlabCalculatorViewModel(
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
        viewModel.onWidthChange("3")
        viewModel.onThicknessChange("15")
        viewModel.onMainDiameterChange("12")
        viewModel.onMainSpacingChange("15")
        viewModel.onDistDiameterChange("8")
        viewModel.onDistSpacingChange("20")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(2.25, state.concreteVolumeM3 ?: 0.0, 0.0001)
        assertEquals(21, state.mainBarCount)
        assertEquals(26, state.distributionBarCount)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test validation error states`() = runTest {
        // Zero thickness
        viewModel.onThicknessChange("0")
        assertEquals("Must be > 0", viewModel.uiState.value.thicknessError)
        
        // Invalid spacing
        viewModel.onMainSpacingChange("-10")
        assertEquals("Must be > 0", viewModel.uiState.value.mainSpacingError)
        
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test history restoration`() = runTest {
        val calcId = "slab1"
        val inputs = listOf(
            CalculationInput(key = "length", value = 10.0, unit = "m"),
            CalculationInput(key = "width", value = 8.0, unit = "m"),
            CalculationInput(key = "thickness", value = 20.0, unit = "cm"),
            CalculationInput(key = "mainDiameter", value = 16.0, unit = "mm"),
            CalculationInput(key = "mainSpacing", value = 20.0, unit = "cm"),
            CalculationInput(key = "distDiameter", value = 10.0, unit = "mm"),
            CalculationInput(key = "distSpacing", value = 25.0, unit = "cm")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.SLAB,
            displayName = "Slab quantities",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 150.0,
            resultUnit = "kg",
            timestamp = 6000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("10.0", state.length)
        assertEquals(DimensionUnit.METER, state.lengthUnit)
        assertEquals("20.0", state.thickness)
        assertEquals(DimensionUnit.CENTIMETER, state.thicknessUnit)
        assertTrue(state.isCalculateEnabled)
    }
}
