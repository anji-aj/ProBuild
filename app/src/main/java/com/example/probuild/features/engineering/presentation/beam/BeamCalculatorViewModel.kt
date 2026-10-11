package com.example.probuild.features.engineering.presentation.beam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.BeamCalculator
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

class BeamCalculatorViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeamCalculatorUiState())
    val uiState = _uiState.asStateFlow()

    private val calculator = BeamCalculator()

    // Tracking touches for validation
    private var isLengthTouched = false
    private var isWidthTouched = false
    private var isDepthTouched = false
    private var isMainBarCountTouched = false
    private var isMainBarDiameterTouched = false
    private var isStirrupDiameterTouched = false
    private var isStirrupSpacingTouched = false

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
            if (calculation != null && calculation.type == CalculationType.BEAM) {
                val inputs = calculation.inputs.associateBy { it.key }

                _uiState.update { state ->
                    state.copy(
                        length = inputs["length"]?.value?.toString() ?: "",
                        lengthUnit = DimensionUnit.fromSymbol(inputs["length"]?.unit ?: "m"),
                        width = inputs["width"]?.value?.toString() ?: "",
                        widthUnit = DimensionUnit.fromSymbol(inputs["width"]?.unit ?: "cm"),
                        depth = inputs["depth"]?.value?.toString() ?: "",
                        depthUnit = DimensionUnit.fromSymbol(inputs["depth"]?.unit ?: "cm"),
                        mainBarCount = inputs["mainBarCount"]?.value?.toInt()?.toString() ?: "",
                        mainBarDiameter = inputs["mainBarDiameter"]?.value?.toString() ?: "",
                        mainBarDiameterUnit = DimensionUnit.fromSymbol(inputs["mainBarDiameter"]?.unit ?: "mm"),
                        stirrupDiameter = inputs["stirrupDiameter"]?.value?.toString() ?: "",
                        stirrupDiameterUnit = DimensionUnit.fromSymbol(inputs["stirrupDiameter"]?.unit ?: "mm"),
                        stirrupSpacing = inputs["stirrupSpacing"]?.value?.toString() ?: "",
                        stirrupSpacingUnit = DimensionUnit.fromSymbol(inputs["stirrupSpacing"]?.unit ?: "mm"),
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
        isLengthTouched = true
        isWidthTouched = true
        isDepthTouched = true
        isMainBarCountTouched = true
        isMainBarDiameterTouched = true
        isStirrupDiameterTouched = true
        isStirrupSpacingTouched = true
    }

    // Input handlers
    fun onLengthChange(v: String) { isLengthTouched = true; _uiState.update { it.copy(length = v) }; validateInputs() }
    fun onLengthUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.length, state.lengthUnit, u)
            state.copy(length = newValue, lengthUnit = u)
        }
        validateInputs()
    }
    fun onWidthChange(v: String) { isWidthTouched = true; _uiState.update { it.copy(width = v) }; validateInputs() }
    fun onWidthUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.width, state.widthUnit, u)
            state.copy(width = newValue, widthUnit = u)
        }
        validateInputs()
    }
    fun onDepthChange(v: String) { isDepthTouched = true; _uiState.update { it.copy(depth = v) }; validateInputs() }
    fun onDepthUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.depth, state.depthUnit, u)
            state.copy(depth = newValue, depthUnit = u)
        }
        validateInputs()
    }
    fun onMainBarCountChange(v: String) { isMainBarCountTouched = true; _uiState.update { it.copy(mainBarCount = v) }; validateInputs() }
    fun onMainBarDiameterChange(v: String) { isMainBarDiameterTouched = true; _uiState.update { it.copy(mainBarDiameter = v) }; validateInputs() }
    fun onMainBarDiameterUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.mainBarDiameter, state.mainBarDiameterUnit, u)
            state.copy(mainBarDiameter = newValue, mainBarDiameterUnit = u)
        }
        validateInputs()
    }
    fun onStirrupDiameterChange(v: String) { isStirrupDiameterTouched = true; _uiState.update { it.copy(stirrupDiameter = v) }; validateInputs() }
    fun onStirrupDiameterUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.stirrupDiameter, state.stirrupDiameterUnit, u)
            state.copy(stirrupDiameter = newValue, stirrupDiameterUnit = u)
        }
        validateInputs()
    }
    fun onStirrupSpacingChange(v: String) { isStirrupSpacingTouched = true; _uiState.update { it.copy(stirrupSpacing = v) }; validateInputs() }
    fun onStirrupSpacingUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.stirrupSpacing, state.stirrupSpacingUnit, u)
            state.copy(stirrupSpacing = newValue, stirrupSpacingUnit = u)
        }
        validateInputs()
    }

    private fun validateInputs() {
        val s = _uiState.value
        val l = s.length.toDoubleOrNull()
        val w = s.width.toDoubleOrNull()
        val d = s.depth.toDoubleOrNull()
        val mc = s.mainBarCount.toIntOrNull()
        val md = s.mainBarDiameter.toDoubleOrNull()
        val sd = s.stirrupDiameter.toDoubleOrNull()
        val ss = s.stirrupSpacing.toDoubleOrNull()

        val lErr = validateNumeric(s.length, isLengthTouched, l)
        val wErr = validateNumeric(s.width, isWidthTouched, w)
        val dErr = validateNumeric(s.depth, isDepthTouched, d)
        val mcErr = validateInt(s.mainBarCount, isMainBarCountTouched, mc)
        val mdErr = validateNumeric(s.mainBarDiameter, isMainBarDiameterTouched, md)
        val sdErr = validateNumeric(s.stirrupDiameter, isStirrupDiameterTouched, sd)
        val ssErr = validateNumeric(s.stirrupSpacing, isStirrupSpacingTouched, ss)

        val isValid = listOf(lErr, wErr, dErr, mcErr, mdErr, sdErr, ssErr).all { it == null } &&
                      l != null && w != null && d != null && mc != null && md != null && sd != null && ss != null

        val result = if (isValid) {
            calculator.calculate(
                l!!, s.lengthUnit, w!!, s.widthUnit, d!!, s.depthUnit,
                mc!!, md!!, s.mainBarDiameterUnit,
                sd!!, s.stirrupDiameterUnit,
                ss!!, s.stirrupSpacingUnit
            )
        } else null

        _uiState.update {
            it.copy(
                lengthError = lErr, widthError = wErr, depthError = dErr,
                mainBarCountError = mcErr, mainBarDiameterError = mdErr,
                stirrupDiameterError = sdErr, stirrupSpacingError = ssErr,
                isCalculateEnabled = isValid,
                concreteVolumeM3 = result?.concreteVolumeM3,
                mainSteelWeightKg = result?.mainSteelWeightKg,
                stirrupWeightKg = result?.stirrupWeightKg,
                totalSteelWeightKg = result?.totalSteelWeightKg,
                stirrupCount = result?.stirrupCount
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

    private fun validateInt(value: String, touched: Boolean, num: Int?): String? {
        if (!touched) return null
        if (value.isBlank()) return "Required"
        if (num == null) return "Invalid integer"
        if (num <= 0) return "Must be > 0"
        return null
    }

    fun saveCalculation() {
        val s = _uiState.value
        val project = s.selectedProject ?: return
        val totalWeight = s.totalSteelWeightKg ?: return

        viewModelScope.launch {
            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                type = CalculationType.BEAM,
                displayName = "Beam Quantities",
                formulaVersion = FORMULA_VERSION,
                inputs = listOf(
                    CalculationInput(key = "length", value = s.length.toDouble(), unit = s.lengthUnit.symbol),
                    CalculationInput(key = "width", value = s.width.toDouble(), unit = s.widthUnit.symbol),
                    CalculationInput(key = "depth", value = s.depth.toDouble(), unit = s.depthUnit.symbol),
                    CalculationInput(key = "mainBarCount", value = s.mainBarCount.toDouble(), unit = "pcs"),
                    CalculationInput(key = "mainBarDiameter", value = s.mainBarDiameter.toDouble(), unit = s.mainBarDiameterUnit.symbol),
                    CalculationInput(key = "stirrupDiameter", value = s.stirrupDiameter.toDouble(), unit = s.stirrupDiameterUnit.symbol),
                    CalculationInput(key = "stirrupSpacing", value = s.stirrupSpacing.toDouble(), unit = s.stirrupSpacingUnit.symbol)
                ),
                resultVal = totalWeight,
                resultUnit = "kg",
                timestamp = System.currentTimeMillis()
            )
            engineeringRepository.insertCalculation(calculation)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
