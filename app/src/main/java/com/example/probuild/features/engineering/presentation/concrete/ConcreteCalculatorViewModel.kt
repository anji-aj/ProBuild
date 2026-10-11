package com.example.probuild.features.engineering.presentation.concrete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.ConcreteVolumeCalculator
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationInput
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.CalculatorViewModelUtils
import com.example.probuild.features.engineering.util.EngineeringConstants.FORMULA_VERSION
import com.example.probuild.features.projects.domain.ProjectsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ConcreteCalculatorViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConcreteCalculatorUiState())
    val uiState = _uiState.asStateFlow()

    private val calculator = ConcreteVolumeCalculator()

    // Tracking if fields have been interacted with for Validation UX
    private var isLengthTouched = false
    private var isWidthTouched = false
    private var isDepthTouched = false

    init {
        loadProjectContext()
    }

    private fun loadProjectContext() {
        viewModelScope.launch {
            combine(
                projectsRepository.getProjects(),
                preferenceManager.selectedProjectId
            ) { projects, selectedId ->
                projects.find { it.id == selectedId } ?: projects.firstOrNull()
            }.collect { project ->
                _uiState.update { it.copy(selectedProject = project) }
            }
        }
    }

    fun loadCalculation(id: String?) {
        if (id == null) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val calculation = engineeringRepository.getCalculationById(id)
            if (calculation != null && calculation.type == CalculationType.CONCRETE) {
                // Restore values and units
                val lengthInput = calculation.inputs.find { it.key == "length" }
                val widthInput = calculation.inputs.find { it.key == "width" }
                val depthInput = calculation.inputs.find { it.key == "depth" }

                _uiState.update { state ->
                    state.copy(
                        length = lengthInput?.value?.toString() ?: "",
                        lengthUnit = DimensionUnit.fromSymbol(lengthInput?.unit ?: "m"),
                        width = widthInput?.value?.toString() ?: "",
                        widthUnit = DimensionUnit.fromSymbol(widthInput?.unit ?: "m"),
                        depth = depthInput?.value?.toString() ?: "",
                        depthUnit = DimensionUnit.fromSymbol(depthInput?.unit ?: "m"),
                        isLoading = false
                    )
                }
                // Once loaded, validate immediately
                isLengthTouched = true
                isWidthTouched = true
                isDepthTouched = true
                validateInputs()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onLengthChange(value: String) {
        isLengthTouched = true
        _uiState.update { it.copy(length = value) }
        validateInputs()
    }

    fun onLengthUnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.length, state.lengthUnit, unit)
            state.copy(length = newValue, lengthUnit = unit)
        }
        validateInputs()
    }

    fun onWidthChange(value: String) {
        isWidthTouched = true
        _uiState.update { it.copy(width = value) }
        validateInputs()
    }

    fun onWidthUnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.width, state.widthUnit, unit)
            state.copy(width = newValue, widthUnit = unit)
        }
        validateInputs()
    }

    fun onDepthChange(value: String) {
        isDepthTouched = true
        _uiState.update { it.copy(depth = value) }
        validateInputs()
    }

    fun onDepthUnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.depth, state.depthUnit, unit)
            state.copy(depth = newValue, depthUnit = unit)
        }
        validateInputs()
    }

    private fun validateInputs() {
        val state = _uiState.value
        
        val l = state.length.toDoubleOrNull()
        val w = state.width.toDoubleOrNull()
        val d = state.depth.toDoubleOrNull()

        val lengthError = when {
            !isLengthTouched -> null
            state.length.isBlank() -> "Length is required"
            l == null -> "Invalid number"
            l <= 0 -> "Must be greater than zero"
            else -> null
        }

        val widthError = when {
            !isWidthTouched -> null
            state.width.isBlank() -> "Width is required"
            w == null -> "Invalid number"
            w <= 0 -> "Must be greater than zero"
            else -> null
        }

        val depthError = when {
            !isDepthTouched -> null
            state.depth.isBlank() -> "Depth is required"
            d == null -> "Invalid number"
            d <= 0 -> "Must be greater than zero"
            else -> null
        }

        val isValid = lengthError == null && widthError == null && depthError == null &&
                l != null && w != null && d != null

        val result = if (isValid) {
            calculator.calculate(
                l!!, state.lengthUnit,
                w!!, state.widthUnit,
                d!!, state.depthUnit
            )
        } else null

        _uiState.update {
            it.copy(
                lengthError = lengthError,
                widthError = widthError,
                depthError = depthError,
                isCalculateEnabled = isValid,
                resultM3 = result?.cubicMeters,
                resultFt3 = result?.cubicFeet
            )
        }
    }

    fun saveCalculation() {
        val state = _uiState.value
        val project = state.selectedProject ?: return
        val l = state.length.toDoubleOrNull() ?: return
        val w = state.width.toDoubleOrNull() ?: return
        val d = state.depth.toDoubleOrNull() ?: return
        val m3 = state.resultM3 ?: return

        viewModelScope.launch {
            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                type = CalculationType.CONCRETE,
                displayName = "Concrete Volume",
                formulaVersion = FORMULA_VERSION,
                inputs = listOf(
                    CalculationInput(key = "length", value = l, unit = state.lengthUnit.symbol),
                    CalculationInput(key = "width", value = w, unit = state.widthUnit.symbol),
                    CalculationInput(key = "depth", value = d, unit = state.depthUnit.symbol)
                ),
                resultVal = m3,
                resultUnit = "m³",
                timestamp = System.currentTimeMillis()
            )
            engineeringRepository.insertCalculation(calculation)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
