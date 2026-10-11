package com.example.probuild.features.engineering.presentation.slab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.SlabCalculator
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

class SlabCalculatorViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SlabCalculatorUiState())
    val uiState = _uiState.asStateFlow()

    private val calculator = SlabCalculator()

    // Tracking touches for validation
    private var isLengthTouched = false
    private var isWidthTouched = false
    private var isThicknessTouched = false
    private var isMainDiameterTouched = false
    private var isMainSpacingTouched = false
    private var isDistDiameterTouched = false
    private var isDistSpacingTouched = false

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
            if (calculation != null && calculation.type == CalculationType.SLAB) {
                val inputs = calculation.inputs.associateBy { it.key }

                _uiState.update { state ->
                    state.copy(
                        length = inputs["length"]?.value?.toString() ?: "",
                        lengthUnit = DimensionUnit.fromSymbol(inputs["length"]?.unit ?: "m"),
                        width = inputs["width"]?.value?.toString() ?: "",
                        widthUnit = DimensionUnit.fromSymbol(inputs["width"]?.unit ?: "m"),
                        thickness = inputs["thickness"]?.value?.toString() ?: "",
                        thicknessUnit = DimensionUnit.fromSymbol(inputs["thickness"]?.unit ?: "cm"),
                        mainDiameter = inputs["mainDiameter"]?.value?.toString() ?: "",
                        mainDiameterUnit = DimensionUnit.fromSymbol(inputs["mainDiameter"]?.unit ?: "mm"),
                        mainSpacing = inputs["mainSpacing"]?.value?.toString() ?: "",
                        mainSpacingUnit = DimensionUnit.fromSymbol(inputs["mainSpacing"]?.unit ?: "cm"),
                        distDiameter = inputs["distDiameter"]?.value?.toString() ?: "",
                        distDiameterUnit = DimensionUnit.fromSymbol(inputs["distDiameter"]?.unit ?: "mm"),
                        distSpacing = inputs["distSpacing"]?.value?.toString() ?: "",
                        distSpacingUnit = DimensionUnit.fromSymbol(inputs["distSpacing"]?.unit ?: "cm"),
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
        isThicknessTouched = true
        isMainDiameterTouched = true
        isMainSpacingTouched = true
        isDistDiameterTouched = true
        isDistSpacingTouched = true
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
    fun onThicknessChange(v: String) { isThicknessTouched = true; _uiState.update { it.copy(thickness = v) }; validateInputs() }
    fun onThicknessUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.thickness, state.thicknessUnit, u)
            state.copy(thickness = newValue, thicknessUnit = u)
        }
        validateInputs()
    }

    fun onMainDiameterChange(v: String) { isMainDiameterTouched = true; _uiState.update { it.copy(mainDiameter = v) }; validateInputs() }
    fun onMainDiameterUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.mainDiameter, state.mainDiameterUnit, u)
            state.copy(mainDiameter = newValue, mainDiameterUnit = u)
        }
        validateInputs()
    }
    fun onMainSpacingChange(v: String) { isMainSpacingTouched = true; _uiState.update { it.copy(mainSpacing = v) }; validateInputs() }
    fun onMainSpacingUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.mainSpacing, state.mainSpacingUnit, u)
            state.copy(mainSpacing = newValue, mainSpacingUnit = u)
        }
        validateInputs()
    }

    fun onDistDiameterChange(v: String) { isDistDiameterTouched = true; _uiState.update { it.copy(distDiameter = v) }; validateInputs() }
    fun onDistDiameterUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.distDiameter, state.distDiameterUnit, u)
            state.copy(distDiameter = newValue, distDiameterUnit = u)
        }
        validateInputs()
    }
    fun onDistSpacingChange(v: String) { isDistSpacingTouched = true; _uiState.update { it.copy(distSpacing = v) }; validateInputs() }
    fun onDistSpacingUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.distSpacing, state.distSpacingUnit, u)
            state.copy(distSpacing = newValue, distSpacingUnit = u)
        }
        validateInputs()
    }

    private fun validateInputs() {
        val s = _uiState.value
        val l = s.length.toDoubleOrNull()
        val w = s.width.toDoubleOrNull()
        val t = s.thickness.toDoubleOrNull()
        val md = s.mainDiameter.toDoubleOrNull()
        val ms = s.mainSpacing.toDoubleOrNull()
        val dd = s.distDiameter.toDoubleOrNull()
        val ds = s.distSpacing.toDoubleOrNull()

        val lErr = validateNumeric(s.length, isLengthTouched, l)
        val wErr = validateNumeric(s.width, isWidthTouched, w)
        val tErr = validateNumeric(s.thickness, isThicknessTouched, t)
        val mdErr = validateNumeric(s.mainDiameter, isMainDiameterTouched, md)
        val msErr = validateNumeric(s.mainSpacing, isMainSpacingTouched, ms)
        val ddErr = validateNumeric(s.distDiameter, isDistDiameterTouched, dd)
        val dsErr = validateNumeric(s.distSpacing, isDistSpacingTouched, ds)

        val isValid = listOf(lErr, wErr, tErr, mdErr, msErr, ddErr, dsErr).all { it == null } &&
                      l != null && w != null && t != null && md != null && ms != null && dd != null && ds != null

        val result = if (isValid) {
            calculator.calculate(
                l!!, s.lengthUnit, w!!, s.widthUnit, t!!, s.thicknessUnit,
                md!!, s.mainDiameterUnit, ms!!, s.mainSpacingUnit,
                dd!!, s.distDiameterUnit, ds!!, s.distSpacingUnit
            )
        } else null

        _uiState.update {
            it.copy(
                lengthError = lErr, widthError = wErr, thicknessError = tErr,
                mainDiameterError = mdErr, mainSpacingError = msErr,
                distDiameterError = ddErr, distSpacingError = dsErr,
                isCalculateEnabled = isValid,
                concreteVolumeM3 = result?.concreteVolumeM3,
                mainSteelWeightKg = result?.mainSteelWeightKg,
                distributionSteelWeightKg = result?.distributionSteelWeightKg,
                totalSteelWeightKg = result?.totalSteelWeightKg,
                mainBarCount = result?.mainBarCount,
                distributionBarCount = result?.distributionBarCount
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

    fun saveCalculation() {
        val s = _uiState.value
        val project = s.selectedProject ?: return
        val totalWeight = s.totalSteelWeightKg ?: return

        viewModelScope.launch {
            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                type = CalculationType.SLAB,
                displayName = "Slab quantities",
                formulaVersion = FORMULA_VERSION,
                inputs = listOf(
                    CalculationInput(key = "length", value = s.length.toDouble(), unit = s.lengthUnit.symbol),
                    CalculationInput(key = "width", value = s.width.toDouble(), unit = s.widthUnit.symbol),
                    CalculationInput(key = "thickness", value = s.thickness.toDouble(), unit = s.thicknessUnit.symbol),
                    CalculationInput(key = "mainDiameter", value = s.mainDiameter.toDouble(), unit = s.mainDiameterUnit.symbol),
                    CalculationInput(key = "mainSpacing", value = s.mainSpacing.toDouble(), unit = s.mainSpacingUnit.symbol),
                    CalculationInput(key = "distDiameter", value = s.distDiameter.toDouble(), unit = s.distDiameterUnit.symbol),
                    CalculationInput(key = "distSpacing", value = s.distSpacing.toDouble(), unit = s.distSpacingUnit.symbol)
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
