package com.example.probuild.features.engineering.presentation.volume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.VolumeCalculator
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

class VolumeCalculatorViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VolumeCalculatorUiState())
    val uiState = _uiState.asStateFlow()

    private val calculator = VolumeCalculator()

    private var isDim1Touched = false
    private var isDim2Touched = false
    private var isDim3Touched = false

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
            if (calculation != null && calculation.type == CalculationType.VOLUME) {
                val inputs = calculation.inputs.associateBy { it.key }
                val shapeStr = inputs["shape"]?.unit ?: VolumeCalculator.Shape.RECTANGULAR_PRISM.name
                val shape = try { VolumeCalculator.Shape.valueOf(shapeStr) } catch(e: Exception) { VolumeCalculator.Shape.RECTANGULAR_PRISM }

                _uiState.update { state ->
                    state.copy(
                        selectedShape = shape,
                        dim1 = inputs["dim1"]?.value?.toString() ?: "",
                        dim1Unit = DimensionUnit.fromSymbol(inputs["dim1"]?.unit ?: "m"),
                        dim2 = inputs["dim2"]?.value?.toString() ?: "",
                        dim2Unit = DimensionUnit.fromSymbol(inputs["dim2"]?.unit ?: "m"),
                        dim3 = inputs["dim3"]?.value?.toString() ?: "",
                        dim3Unit = DimensionUnit.fromSymbol(inputs["dim3"]?.unit ?: "m"),
                        isLoading = false
                    )
                }
                markAllTouched()
                validateInputs()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun markAllTouched() {
        isDim1Touched = true
        isDim2Touched = true
        isDim3Touched = true
    }

    fun onShapeSelected(shape: VolumeCalculator.Shape) {
        _uiState.update { it.copy(selectedShape = shape) }
        isDim1Touched = false
        isDim2Touched = false
        isDim3Touched = false
        validateInputs()
    }

    fun onDim1Change(value: String) {
        isDim1Touched = true
        _uiState.update { it.copy(dim1 = value) }
        validateInputs()
    }

    fun onDim1UnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.dim1, state.dim1Unit, unit)
            state.copy(dim1 = newValue, dim1Unit = unit)
        }
        validateInputs()
    }

    fun onDim2Change(value: String) {
        isDim2Touched = true
        _uiState.update { it.copy(dim2 = value) }
        validateInputs()
    }

    fun onDim2UnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.dim2, state.dim2Unit, unit)
            state.copy(dim2 = newValue, dim2Unit = unit)
        }
        validateInputs()
    }

    fun onDim3Change(value: String) {
        isDim3Touched = true
        _uiState.update { it.copy(dim3 = value) }
        validateInputs()
    }

    fun onDim3UnitChange(unit: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.dim3, state.dim3Unit, unit)
            state.copy(dim3 = newValue, dim3Unit = unit)
        }
        validateInputs()
    }

    private fun validateInputs() {
        val state = _uiState.value
        val d1 = state.dim1.toDoubleOrNull()
        val d2 = state.dim2.toDoubleOrNull()
        val d3 = state.dim3.toDoubleOrNull()

        val dim1Error = validateNumeric(state.dim1, isDim1Touched, d1)
        val dim2Error = if (requiresDim2(state.selectedShape)) {
            validateNumeric(state.dim2, isDim2Touched, d2)
        } else null
        val dim3Error = if (requiresDim3(state.selectedShape)) {
            validateNumeric(state.dim3, isDim3Touched, d3)
        } else null

        val isValid = dim1Error == null && dim2Error == null && dim3Error == null && 
                      d1 != null && 
                      (!requiresDim2(state.selectedShape) || d2 != null) &&
                      (!requiresDim3(state.selectedShape) || d3 != null)

        val result = if (isValid) {
            when (state.selectedShape) {
                VolumeCalculator.Shape.RECTANGULAR_PRISM -> calculator.calculateRectangularPrism(d1!!, state.dim1Unit, d2!!, state.dim2Unit, d3!!, state.dim3Unit)
                VolumeCalculator.Shape.CUBE -> calculator.calculateCube(d1!!, state.dim1Unit)
                VolumeCalculator.Shape.CYLINDER -> calculator.calculateCylinder(d1!!, state.dim1Unit, d2!!, state.dim2Unit)
                VolumeCalculator.Shape.SPHERE -> calculator.calculateSphere(d1!!, state.dim1Unit)
            }
        } else null

        _uiState.update {
            it.copy(
                dim1Error = dim1Error,
                dim2Error = dim2Error,
                dim3Error = dim3Error,
                isCalculateEnabled = isValid,
                resultM3 = result?.volumeM3,
                resultFt3 = result?.volumeFt3
            )
        }
    }

    private fun validateNumeric(value: String, touched: Boolean, num: Double?): String? {
        if (!touched) return null
        if (value.isBlank()) return "Required"
        if (num == null) return "Invalid number"
        if (num <= 0) return "Must be > 0"
        return null
    }

    private fun requiresDim2(shape: VolumeCalculator.Shape): Boolean = when (shape) {
        VolumeCalculator.Shape.RECTANGULAR_PRISM, VolumeCalculator.Shape.CYLINDER -> true
        else -> false
    }

    private fun requiresDim3(shape: VolumeCalculator.Shape): Boolean = when (shape) {
        VolumeCalculator.Shape.RECTANGULAR_PRISM -> true
        else -> false
    }

    fun saveCalculation() {
        val state = _uiState.value
        val project = state.selectedProject ?: return
        val d1 = state.dim1.toDoubleOrNull() ?: return
        val volume = state.resultM3 ?: return

        viewModelScope.launch {
            val inputs = mutableListOf(
                CalculationInput(key = "shape", value = 0.0, unit = state.selectedShape.name),
                CalculationInput(key = "dim1", value = d1, unit = state.dim1Unit.symbol)
            )
            if (requiresDim2(state.selectedShape)) {
                val d2 = state.dim2.toDoubleOrNull() ?: 0.0
                inputs.add(CalculationInput(key = "dim2", value = d2, unit = state.dim2Unit.symbol))
            }
            if (requiresDim3(state.selectedShape)) {
                val d3 = state.dim3.toDoubleOrNull() ?: 0.0
                inputs.add(CalculationInput(key = "dim3", value = d3, unit = state.dim3Unit.symbol))
            }

            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                type = CalculationType.VOLUME,
                displayName = "Volume (${state.selectedShape.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }})",
                formulaVersion = FORMULA_VERSION,
                inputs = inputs,
                resultVal = volume,
                resultUnit = "m³",
                timestamp = System.currentTimeMillis()
            )
            engineeringRepository.insertCalculation(calculation)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
