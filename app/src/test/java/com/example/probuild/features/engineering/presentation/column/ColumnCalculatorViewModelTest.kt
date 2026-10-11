package com.example.probuild.features.engineering.presentation.column

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
class ColumnCalculatorViewModelTest {

    private lateinit var viewModel: ColumnCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = ColumnCalculatorViewModel(
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
        viewModel.onHeightChange("3")
        viewModel.onWidthChange("40")
        viewModel.onDepthChange("40")
        viewModel.onMainBarCountChange("4")
        viewModel.onMainBarDiameterChange("16")
        viewModel.onTieDiameterChange("8")
        viewModel.onTieSpacingChange("200")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(0.48, state.concreteVolumeM3 ?: 0.0, 0.0001)
        assertEquals(16, state.tieCount)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test validation error states`() = runTest {
        // Zero height
        viewModel.onHeightChange("0")
        assertEquals("Must be > 0", viewModel.uiState.value.heightError)
        
        // Negative count
        viewModel.onMainBarCountChange("-2")
        assertEquals("Must be > 0", viewModel.uiState.value.mainBarCountError)
        
        // Invalid spacing
        viewModel.onTieSpacingChange("abc")
        assertEquals("Invalid number", viewModel.uiState.value.tieSpacingError)
        
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test history restoration`() = runTest {
        val calcId = "col1"
        val inputs = listOf(
            CalculationInput(key = "height", value = 4.0, unit = "m"),
            CalculationInput(key = "width", value = 30.0, unit = "cm"),
            CalculationInput(key = "depth", value = 30.0, unit = "cm"),
            CalculationInput(key = "mainBarCount", value = 8.0, unit = "pcs"),
            CalculationInput(key = "mainBarDiameter", value = 25.0, unit = "mm"),
            CalculationInput(key = "tieDiameter", value = 10.0, unit = "mm"),
            CalculationInput(key = "tieSpacing", value = 150.0, unit = "mm")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.COLUMN,
            displayName = "Column Quantities",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 100.0,
            resultUnit = "kg",
            timestamp = 5000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("4.0", state.height)
        assertEquals(DimensionUnit.METER, state.heightUnit)
        assertEquals("30.0", state.width)
        assertEquals(DimensionUnit.CENTIMETER, state.widthUnit)
        assertEquals("8", state.mainBarCount)
        assertEquals(DimensionUnit.MILLIMETER, state.mainBarDiameterUnit)
        assertTrue(state.isCalculateEnabled)
    }
}
