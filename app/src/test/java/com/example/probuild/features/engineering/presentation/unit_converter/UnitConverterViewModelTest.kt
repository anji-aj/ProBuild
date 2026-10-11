package com.example.probuild.features.engineering.presentation.unit_converter

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.EngineeringUnitConverter
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationInput
import com.example.probuild.features.engineering.domain.model.CalculationType
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
class UnitConverterViewModelTest {

    private lateinit var viewModel: UnitConverterViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = UnitConverterViewModel(
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
        viewModel.onCategorySelected(EngineeringUnitConverter.Category.LENGTH)
        viewModel.onFromUnitChange("m")
        viewModel.onToUnitChange("ft")
        viewModel.onInputValueChange("10")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(32.8084, state.convertedResult ?: 0.0, 0.001)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test swap units`() = runTest {
        viewModel.onCategorySelected(EngineeringUnitConverter.Category.LENGTH)
        viewModel.onFromUnitChange("m")
        viewModel.onToUnitChange("ft")
        viewModel.onInputValueChange("1")
        
        viewModel.swapUnits()
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("ft", state.fromUnitSymbol)
        assertEquals("m", state.toUnitSymbol)
        assertEquals(0.3048, state.convertedResult ?: 0.0, 0.0001)
    }

    @Test
    fun `test validation error states`() = runTest {
        viewModel.onInputValueChange("abc")
        assertEquals("Invalid number", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test repeat functionality restores exactly`() = runTest {
        val calcId = "conv1"
        val inputs = listOf(
            CalculationInput(key = "category", value = 0.0, unit = "MASS"),
            CalculationInput(key = "value", value = 5.0, unit = "kg")
        )
        val calculation = Calculation(
            id = calcId,
            projectId = "p1",
            type = CalculationType.UNIT_CONVERTER,
            displayName = "Unit Conversion",
            formulaVersion = "1",
            inputs = inputs,
            resultVal = 11.0231,
            resultUnit = "lb",
            timestamp = 8000L
        )
        coEvery { engineeringRepository.getCalculationById(calcId) } returns calculation

        viewModel.loadCalculation(calcId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(EngineeringUnitConverter.Category.MASS, state.selectedCategory)
        assertEquals("5.0", state.inputValue)
        assertEquals("kg", state.fromUnitSymbol)
        assertEquals("lb", state.toUnitSymbol)
        assertTrue(state.isCalculateEnabled)
    }
}
