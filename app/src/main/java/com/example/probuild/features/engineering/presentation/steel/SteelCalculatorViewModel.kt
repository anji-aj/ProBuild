package com.example.probuild.features.engineering.presentation.steel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.SteelWeightCalculator
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

class SteelCalculatorViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SteelCalculatorUiState())
    val uiState = _uiState.asStateFlow()

    private val calculator = SteelWeightCalculator()

    private var isDiameterTouched = false
    private var isLengthTouched = false
    private var isCountTouched = false

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
            if (calculation != null && calculation.type == CalculationType.STEEL) {
                val inputs = calculation.inputs.associateBy { it.key }

                _uiState.update { state ->
                    state.copy(
                        barDiameter = inputs["diameter"]?.value?.toString() ?: "",
                        diameterUnit = DimensionUnit.fromSymbol(inputs["diameter"]?.unit ?: "mm"),
                        barLength = inputs["length"]?.value?.toString() ?: "",
                        lengthUnit = DimensionUnit.fromSymbol(inputs["length"]?.unit ?: "m"),
                        numberOfBars = inputs["count"]?.value?.toInt()?.toString() ?: "1",
                        isLoading = false
                    )
                }
                isDiameterTouched = true
                isLengthTouched = true
                isCountTouched = true
                validateInputs()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDiameterChange(value: String) {
        isDiameterTouched = true
        _uiState.update { it.copy(barDiameter = value) }
        validateInputs()
    }

    fun onDiameterUnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.barDiameter, state.diameterUnit, unit)
            state.copy(barDiameter = newValue, diameterUnit = unit)
        }
        validateInputs()
    }

    fun onLengthChange(value: String) {
        isLengthTouched = true
        _uiState.update { it.copy(barLength = value) }
        validateInputs()
    }

    fun onLengthUnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.barLength, state.lengthUnit, unit)
            state.copy(barLength = newValue, lengthUnit = unit)
        }
        validateInputs()
    }

    fun onCountChange(value: String) {
        isCountTouched = true
        _uiState.update { it.copy(numberOfBars = value) }
        validateInputs()
    }

    private fun validateInputs() {
        val state = _uiState.value
        val d = state.barDiameter.toDoubleOrNull()
        val l = state.barLength.toDoubleOrNull()
        val n = state.numberOfBars.toIntOrNull()

        val diameterError = when {
            !isDiameterTouched -> null
            state.barDiameter.isBlank() -> "Diameter is required"
            d == null -> "Invalid number"
            d <= 0 -> "Must be greater than zero"
            else -> null
        }

        val lengthError = when {
            !isLengthTouched -> null
            state.barLength.isBlank() -> "Length is required"
            l == null -> "Invalid number"
            l <= 0 -> "Must be greater than zero"
            else -> null
        }

        val countError = when {
            !isCountTouched -> null
            state.numberOfBars.isBlank() -> "Count is required"
            n == null -> "Invalid number"
            n <= 0 -> "Must be at least 1"
            else -> null
        }

        val isValid = diameterError == null && lengthError == null && countError == null &&
                d != null && l != null && n != null

        val result = if (isValid) {
            calculator.calculate(
                d!!, state.diameterUnit,
                l!!, state.lengthUnit,
                n!!
            )
        } else null

        _uiState.update {
            it.copy(
                diameterError = diameterError,
                lengthError = lengthError,
                countError = countError,
                isCalculateEnabled = isValid,
                resultWeightKg = result?.weightKg,
                resultVolumeM3 = result?.volumeM3,
                resultTotalBars = result?.totalBars
            )
        }
    }

    fun saveCalculation() {
        val state = _uiState.value
        val project = state.selectedProject ?: return
        val d = state.barDiameter.toDoubleOrNull() ?: return
        val l = state.barLength.toDoubleOrNull() ?: return
        val n = state.numberOfBars.toIntOrNull() ?: return
        val weight = state.resultWeightKg ?: return

        viewModelScope.launch {
            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                type = CalculationType.STEEL,
                displayName = "Steel Weight",
                formulaVersion = FORMULA_VERSION,
                inputs = listOf(
                    CalculationInput(key = "diameter", value = d, unit = state.diameterUnit.symbol),
                    CalculationInput(key = "length", value = l, unit = state.lengthUnit.symbol),
                    CalculationInput(key = "count", value = n.toDouble(), unit = "pcs")
                ),
                resultVal = weight,
                resultUnit = "kg",
                timestamp = System.currentTimeMillis()
            )
            engineeringRepository.insertCalculation(calculation)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
