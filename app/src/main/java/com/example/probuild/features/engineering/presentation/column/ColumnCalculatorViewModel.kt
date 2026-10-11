package com.example.probuild.features.engineering.presentation.column

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.ColumnCalculator
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

class ColumnCalculatorViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColumnCalculatorUiState())
    val uiState = _uiState.asStateFlow()

    private val calculator = ColumnCalculator()

    // Tracking touches for validation
    private var isHeightTouched = false
    private var isWidthTouched = false
    private var isDepthTouched = false
    private var isMainBarCountTouched = false
    private var isMainBarDiameterTouched = false
    private var isTieDiameterTouched = false
    private var isTieSpacingTouched = false

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
            if (calculation != null && calculation.type == CalculationType.COLUMN) {
                val inputs = calculation.inputs.associateBy { it.key }

                _uiState.update { state ->
                    state.copy(
                        height = inputs["height"]?.value?.toString() ?: "",
                        heightUnit = DimensionUnit.fromSymbol(inputs["height"]?.unit ?: "m"),
                        width = inputs["width"]?.value?.toString() ?: "",
                        widthUnit = DimensionUnit.fromSymbol(inputs["width"]?.unit ?: "cm"),
                        depth = inputs["depth"]?.value?.toString() ?: "",
                        depthUnit = DimensionUnit.fromSymbol(inputs["depth"]?.unit ?: "cm"),
                        mainBarCount = inputs["mainBarCount"]?.value?.toInt()?.toString() ?: "",
                        mainBarDiameter = inputs["mainBarDiameter"]?.value?.toString() ?: "",
                        mainBarDiameterUnit = DimensionUnit.fromSymbol(inputs["mainBarDiameter"]?.unit ?: "mm"),
                        tieDiameter = inputs["tieDiameter"]?.value?.toString() ?: "",
                        tieDiameterUnit = DimensionUnit.fromSymbol(inputs["tieDiameter"]?.unit ?: "mm"),
                        tieSpacing = inputs["tieSpacing"]?.value?.toString() ?: "",
                        tieSpacingUnit = DimensionUnit.fromSymbol(inputs["tieSpacing"]?.unit ?: "mm"),
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
        isHeightTouched = true
        isWidthTouched = true
        isDepthTouched = true
        isMainBarCountTouched = true
        isMainBarDiameterTouched = true
        isTieDiameterTouched = true
        isTieSpacingTouched = true
    }

    // Input handlers
    fun onHeightChange(v: String) { isHeightTouched = true; _uiState.update { it.copy(height = v) }; validateInputs() }
    fun onHeightUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.height, state.heightUnit, u)
            state.copy(height = newValue, heightUnit = u)
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
    fun onTieDiameterChange(v: String) { isTieDiameterTouched = true; _uiState.update { it.copy(tieDiameter = v) }; validateInputs() }
    fun onTieDiameterUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.tieDiameter, state.tieDiameterUnit, u)
            state.copy(tieDiameter = newValue, tieDiameterUnit = u)
        }
        validateInputs()
    }
    fun onTieSpacingChange(v: String) { isTieSpacingTouched = true; _uiState.update { it.copy(tieSpacing = v) }; validateInputs() }
    fun onTieSpacingUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.tieSpacing, state.tieSpacingUnit, u)
            state.copy(tieSpacing = newValue, tieSpacingUnit = u)
        }
        validateInputs()
    }

    private fun validateInputs() {
        val s = _uiState.value
        val h = s.height.toDoubleOrNull()
        val w = s.width.toDoubleOrNull()
        val d = s.depth.toDoubleOrNull()
        val mc = s.mainBarCount.toIntOrNull()
        val md = s.mainBarDiameter.toDoubleOrNull()
        val td = s.tieDiameter.toDoubleOrNull()
        val ts = s.tieSpacing.toDoubleOrNull()

        val hErr = validateNumeric(s.height, isHeightTouched, h)
        val wErr = validateNumeric(s.width, isWidthTouched, w)
        val dErr = validateNumeric(s.depth, isDepthTouched, d)
        val mcErr = validateInt(s.mainBarCount, isMainBarCountTouched, mc)
        val mdErr = validateNumeric(s.mainBarDiameter, isMainBarDiameterTouched, md)
        val tdErr = validateNumeric(s.tieDiameter, isTieDiameterTouched, td)
        val tsErr = validateNumeric(s.tieSpacing, isTieSpacingTouched, ts)

        val isValid = listOf(hErr, wErr, dErr, mcErr, mdErr, tdErr, tsErr).all { it == null } &&
                      h != null && w != null && d != null && mc != null && md != null && td != null && ts != null

        val result = if (isValid) {
            calculator.calculate(
                h!!, s.heightUnit, w!!, s.widthUnit, d!!, s.depthUnit,
                mc!!, md!!, s.mainBarDiameterUnit,
                td!!, s.tieDiameterUnit,
                ts!!, s.tieSpacingUnit
            )
        } else null

        _uiState.update {
            it.copy(
                heightError = hErr, widthError = wErr, depthError = dErr,
                mainBarCountError = mcErr, mainBarDiameterError = mdErr,
                tieDiameterError = tdErr, tieSpacingError = tsErr,
                isCalculateEnabled = isValid,
                concreteVolumeM3 = result?.concreteVolumeM3,
                mainSteelWeightKg = result?.mainSteelWeightKg,
                tieWeightKg = result?.tieWeightKg,
                totalSteelWeightKg = result?.totalSteelWeightKg,
                tieCount = result?.tieCount
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
                type = CalculationType.COLUMN,
                displayName = "Column Quantities",
                formulaVersion = FORMULA_VERSION,
                inputs = listOf(
                    CalculationInput(key = "height", value = s.height.toDouble(), unit = s.heightUnit.symbol),
                    CalculationInput(key = "width", value = s.width.toDouble(), unit = s.widthUnit.symbol),
                    CalculationInput(key = "depth", value = s.depth.toDouble(), unit = s.depthUnit.symbol),
                    CalculationInput(key = "mainBarCount", value = s.mainBarCount.toDouble(), unit = "pcs"),
                    CalculationInput(key = "mainBarDiameter", value = s.mainBarDiameter.toDouble(), unit = s.mainBarDiameterUnit.symbol),
                    CalculationInput(key = "tieDiameter", value = s.tieDiameter.toDouble(), unit = s.tieDiameterUnit.symbol),
                    CalculationInput(key = "tieSpacing", value = s.tieSpacing.toDouble(), unit = s.tieSpacingUnit.symbol)
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
