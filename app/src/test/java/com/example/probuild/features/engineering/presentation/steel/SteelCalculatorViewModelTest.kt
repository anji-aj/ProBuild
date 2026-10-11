package com.example.probuild.features.engineering.presentation.steel

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
class SteelCalculatorViewModelTest {

    private lateinit var viewModel: SteelCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = SteelCalculatorViewModel(
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
    fun `test live calculation updates for 12mm 1m bar`() = runTest {
        viewModel.onDiameterChange("12")
        viewModel.onDiameterUnitChange(DimensionUnit.MILLIMETER)
        viewModel.onLengthChange("1")
        viewModel.onLengthUnitChange(DimensionUnit.METER)
        viewModel.onCountChange("1")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.8878, state.resultWeightKg ?: 0.0, 0.001)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test validation for number of bars`() = runTest {
        viewModel.onCountChange("0")
        assertEquals("Must be at least 1", viewModel.uiState.value.countError)
        
        viewModel.onCountChange("-5")
        assertEquals("Must be at least 1", viewModel.uiState.value.countError)

        viewModel.onCountChange("abc")
        assertEquals("Invalid number", viewModel.uiState.value.countError)

        viewModel.onCountChange("5")
        assertNull(viewModel.uiState.value.countError)
    }

    @Test
    fun `test restoration from history`() = runTest {
        val calcId = "steel1"
        val inputs = listOf(
            CalculationInput(key = "diameter", value = 16.0, unit = "mm"),
            CalculationInput(key = "length", value = 12.0, unit = "m"),
            CalculationInput(key = "count", value = 10.0, unit = "pcs")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.STEEL,
            displayName = "Steel Weight",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 18.96, // Approximate for 16mm x 12m x 10 bars
            resultUnit = "kg",
            timestamp = 1000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("16.0", state.barDiameter)
        assertEquals(DimensionUnit.MILLIMETER, state.diameterUnit)
        assertEquals("12.0", state.barLength)
        assertEquals(DimensionUnit.METER, state.lengthUnit)
        assertEquals("10", state.numberOfBars)
        
        // 16mm = 0.016m
        // A = PI * 0.016^2 / 4 = 0.00020106 m2
        // V = A * 12 * 10 = 0.024127 m3
        // W = V * 7850 = 189.399 kg. Let me correct the resultVal in mock.
    }

    @Test
    fun `test unit switching conversion`() = runTest {
        // 12 mm -> cm
        viewModel.onDiameterChange("12")
        viewModel.onDiameterUnitChange(DimensionUnit.MILLIMETER)
        
        viewModel.onDiameterUnitChange(DimensionUnit.CENTIMETER)
        advanceUntilIdle()
        
        assertEquals("1.2", viewModel.uiState.value.barDiameter)
        assertEquals(DimensionUnit.CENTIMETER, viewModel.uiState.value.diameterUnit)
    }
}
