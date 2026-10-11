package com.example.probuild.features.engineering.presentation.volume

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.VolumeCalculator
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
class VolumeCalculatorViewModelTest {

    private lateinit var viewModel: VolumeCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = VolumeCalculatorViewModel(
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
    fun `test live updates for Cylinder`() = runTest {
        viewModel.onShapeSelected(VolumeCalculator.Shape.CYLINDER)
        // r = 1m, h = 2m
        viewModel.onDim1Change("1")
        viewModel.onDim2Change("2")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(6.283185, state.resultM3 ?: 0.0, 0.000001)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test validation error states`() = runTest {
        viewModel.onShapeSelected(VolumeCalculator.Shape.CUBE)
        
        // Negative
        viewModel.onDim1Change("-5")
        assertEquals("Must be > 0", viewModel.uiState.value.dim1Error)
        
        // Zero
        viewModel.onDim1Change("0")
        assertEquals("Must be > 0", viewModel.uiState.value.dim1Error)
        
        // Non-numeric
        viewModel.onDim1Change("xyz")
        assertEquals("Invalid number", viewModel.uiState.value.dim1Error)
        
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test shape switching resets validation state`() = runTest {
        viewModel.onShapeSelected(VolumeCalculator.Shape.RECTANGULAR_PRISM)
        viewModel.onDim1Change("")
        assertNotNull(viewModel.uiState.value.dim1Error)
        
        viewModel.onShapeSelected(VolumeCalculator.Shape.SPHERE)
        // dim1 is still empty but untouched state resets errors
        assertNull(viewModel.uiState.value.dim1Error)
    }

    @Test
    fun `test repeat functionality restores shape and units`() = runTest {
        val calcId = "vol1"
        val inputs = listOf(
            CalculationInput(key = "shape", value = 0.0, unit = "SPHERE"),
            CalculationInput(key = "dim1", value = 2.5, unit = "ft")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.VOLUME,
            displayName = "Volume (Sphere)",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 1.84,
            resultUnit = "m³",
            timestamp = 3000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VolumeCalculator.Shape.SPHERE, state.selectedShape)
        assertEquals("2.5", state.dim1)
        assertEquals(DimensionUnit.FEET, state.dim1Unit)
        assertTrue(state.isCalculateEnabled)
    }
}
