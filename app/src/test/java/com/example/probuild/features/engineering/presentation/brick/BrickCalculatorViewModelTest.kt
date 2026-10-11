package com.example.probuild.features.engineering.presentation.brick

import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.ProjectsRepository
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
class BrickCalculatorViewModelTest {

    private lateinit var viewModel: BrickCalculatorViewModel
    private val engineeringRepository = mockk<EngineeringRepository>(relaxed = true)
    private val projectsRepository = mockk<ProjectsRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { projectsRepository.getProjects() } returns flowOf(emptyList())
        every { preferenceManager.selectedProjectId } returns flowOf(null)
        
        viewModel = BrickCalculatorViewModel(
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
    fun `test live calculation updates`() = runTest {
        // Wall: 5m x 3m x 0.2m = 3m3
        viewModel.onWallLengthChange("5")
        viewModel.onWallHeightChange("3")
        viewModel.onWallThicknessChange("0.2")

        // Brick: 19cm x 9cm x 9cm
        viewModel.onBrickLengthChange("19")
        viewModel.onBrickWidthChange("9")
        viewModel.onBrickHeightChange("9")

        // Mortar: 1cm
        viewModel.onMortarJointChange("1")

        // Wastage: 5%
        viewModel.onWastageChange("5")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3.0, state.wallVolume ?: 0.0, 0.0001)
        assertEquals(1575, state.totalBricks)
        assertTrue(state.isCalculateEnabled)
    }

    @Test
    fun `test validation error states`() = runTest {
        // Touch wall length and clear it
        viewModel.onWallLengthChange("")
        assertEquals("Required", viewModel.uiState.value.wallLengthError)

        // Enter invalid number
        viewModel.onWallHeightChange("abc")
        assertEquals("Invalid number", viewModel.uiState.value.wallHeightError)

        // Enter zero when not allowed
        viewModel.onWallThicknessChange("0")
        assertEquals("Must be > 0", viewModel.uiState.value.wallThicknessError)
        
        // Enter zero when allowed (mortar joint)
        viewModel.onMortarJointChange("0")
        assertNull(viewModel.uiState.value.mortarJointError)

        assertFalse(viewModel.uiState.value.isCalculateEnabled)
    }

    @Test
    fun `test unit switching conversion`() = runTest {
        // Start with meters (default), change to cm first to establish state
        viewModel.onWallThicknessUnitChange(DimensionUnit.CENTIMETER)
        viewModel.onWallThicknessChange("20") // 20 cm
        
        // Switch to m
        viewModel.onWallThicknessUnitChange(DimensionUnit.METER)
        advanceUntilIdle()
        
        assertEquals("0.2", viewModel.uiState.value.wallThickness)
        assertEquals(DimensionUnit.METER, viewModel.uiState.value.wallThicknessUnit)
    }
}
