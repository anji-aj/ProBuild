package com.example.probuild.features.engineering.presentation.concrete

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationInput
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.domain.model.Project
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
class ConcreteCalculatorViewModelTest {

    private lateinit var viewModel: ConcreteCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = ConcreteCalculatorViewModel(
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
    fun `test restoration of calculation values and units`() = runTest {
        val calcId = "c1"
        val inputList = listOf(
            CalculationInput(key = "length", value = 10.5, unit = "ft"),
            CalculationInput(key = "width", value = 5.0, unit = "m"),
            CalculationInput(key = "depth", value = 20.0, unit = "cm")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.CONCRETE,
            displayName = "Concrete",
            formulaVersion = "1",
            inputs = inputList,
            resultVal = 1.0,
            resultUnit = "m³",
            timestamp = 123456789L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("10.5", state.length)
        assertEquals(DimensionUnit.FEET, state.lengthUnit)
        assertEquals("5.0", state.width)
        assertEquals(DimensionUnit.METER, state.widthUnit)
        assertEquals("20.0", state.depth)
        assertEquals(DimensionUnit.CENTIMETER, state.depthUnit)
        
        // Check calculation restoration (10.5ft * 5m * 20cm)
        // 10.5 * 0.3048 = 3.2004 m
        // 5.0 m
        // 20.0 * 0.01 = 0.2 m
        // Vol = 3.2004 * 5.0 * 0.2 = 3.2004 m³
        assertEquals(3.2004, state.resultM3 ?: 0.0, 0.0001)
    }

    @Test
    fun `test precision of unit conversion`() = runTest {
        // 5m x 2m x 0.2m = 2m3
        viewModel.onLengthChange("5")
        viewModel.onLengthUnitChange(DimensionUnit.METER)
        viewModel.onWidthChange("2")
        viewModel.onWidthUnitChange(DimensionUnit.METER)
        viewModel.onDepthChange("0.2")
        viewModel.onDepthUnitChange(DimensionUnit.METER)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(2.0, state.resultM3 ?: 0.0, 0.0001)
        // 2.0 m3 to ft3 should be ~70.6293
        assertEquals(70.62933, state.resultFt3 ?: 0.0, 0.0001)
    }

    @Test
    fun `test unit switching and value conversion`() = runTest {
        // 5 m -> ft
        viewModel.onLengthChange("5")
        viewModel.onLengthUnitChange(DimensionUnit.METER)
        
        viewModel.onLengthUnitChange(DimensionUnit.FEET)
        advanceUntilIdle()
        
        // 5 * 3.280839895 = 16.4042
        assertEquals("16.4042", viewModel.uiState.value.length)
        assertEquals(DimensionUnit.FEET, viewModel.uiState.value.lengthUnit)
        
        // Switch back: 16.4042 ft -> m
        viewModel.onLengthUnitChange(DimensionUnit.METER)
        advanceUntilIdle()
        assertEquals("5", viewModel.uiState.value.length)
    }
    
    @Test
    fun `test validation UX requirements`() = runTest {
        // Initial state: no errors for untouched fields
        assertNull(viewModel.uiState.value.lengthError)
        
        // Touch field and leave empty
        viewModel.onLengthChange("")
        assertEquals("Length is required", viewModel.uiState.value.lengthError)
        
        // Enter invalid number
        viewModel.onLengthChange("abc")
        assertEquals("Invalid number", viewModel.uiState.value.lengthError)
        
        // Enter negative number
        viewModel.onLengthChange("-1")
        assertEquals("Must be greater than zero", viewModel.uiState.value.lengthError)
        
        // Calculate button disabled
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
        
        // Fill all correctly
        viewModel.onLengthChange("5")
        viewModel.onWidthChange("2")
        viewModel.onDepthChange("0.2")
        assertTrue(viewModel.uiState.value.isCalculateEnabled)
        
        // Clear a previously valid field
        viewModel.onLengthChange("")
        assertEquals("Length is required", viewModel.uiState.value.lengthError)
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }
}
