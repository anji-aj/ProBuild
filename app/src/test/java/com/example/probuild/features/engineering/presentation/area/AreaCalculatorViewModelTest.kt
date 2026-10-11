package com.example.probuild.features.engineering.presentation.area

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.AreaCalculator
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
class AreaCalculatorViewModelTest {

    private lateinit var viewModel: AreaCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = AreaCalculatorViewModel(
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
    fun `test live updates and validation for Rectangle`() = runTest {
        viewModel.onShapeSelected(AreaCalculator.Shape.RECTANGLE)
        viewModel.onDim1Change("5")
        viewModel.onDim2Change("3")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(15.0, state.resultM2 ?: 0.0, 0.0001)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test validation error states`() = runTest {
        viewModel.onShapeSelected(AreaCalculator.Shape.RECTANGLE)
        
        // Negative input
        viewModel.onDim1Change("-1")
        assertEquals("Must be > 0", viewModel.uiState.value.dim1Error)
        
        // Zero input
        viewModel.onDim2Change("0")
        assertEquals("Must be > 0", viewModel.uiState.value.dim2Error)
        
        // Non-numeric
        viewModel.onDim1Change("abc")
        assertEquals("Invalid number", viewModel.uiState.value.dim1Error)
        
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test shape switching resets validation`() = runTest {
        viewModel.onShapeSelected(AreaCalculator.Shape.RECTANGLE)
        viewModel.onDim1Change("")
        assertNotNull(viewModel.uiState.value.dim1Error)
        
        viewModel.onShapeSelected(AreaCalculator.Shape.CIRCLE)
        // dim1 should still be "", but untouched state resets errors until touched again
        assertNull(viewModel.uiState.value.dim1Error)
    }

    @Test
    fun `test restoration from history includes shape and units`() = runTest {
        val calcId = "area1"
        val inputs = listOf(
            CalculationInput(key = "shape", value = 0.0, unit = "TRIANGLE"),
            CalculationInput(key = "dim1", value = 10.0, unit = "cm"),
            CalculationInput(key = "dim2", value = 5.0, unit = "ft")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.AREA,
            displayName = "Area (Triangle)",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 0.5,
            resultUnit = "m²",
            timestamp = 2000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AreaCalculator.Shape.TRIANGLE, state.selectedShape)
        assertEquals("10.0", state.dim1)
        assertEquals(DimensionUnit.CENTIMETER, state.dim1Unit)
        assertEquals("5.0", state.dim2)
        assertEquals(DimensionUnit.FEET, state.dim2Unit)
        assertTrue(state.isCalculateEnabled)
    }
}
