package com.example.probuild.features.engineering.presentation.brick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.BrickCalculator
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

class BrickCalculatorViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrickCalculatorUiState())
    val uiState = _uiState.asStateFlow()

    private val calculator = BrickCalculator()

    // Tracking touches for validation
    private var isWallLengthTouched = false
    private var isWallHeightTouched = false
    private var isWallThicknessTouched = false
    private var isBrickLengthTouched = false
    private var isBrickWidthTouched = false
    private var isBrickHeightTouched = false
    private var isMortarJointTouched = false
    private var isWastageTouched = false

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
            if (calculation != null && calculation.type == CalculationType.BRICK) {
                val inputs = calculation.inputs.associateBy { it.key }

                _uiState.update { state ->
                    state.copy(
                        wallLength = inputs["wallLength"]?.value?.toString() ?: "",
                        wallLengthUnit = DimensionUnit.fromSymbol(inputs["wallLength"]?.unit ?: "m"),
                        wallHeight = inputs["wallHeight"]?.value?.toString() ?: "",
                        wallHeightUnit = DimensionUnit.fromSymbol(inputs["wallHeight"]?.unit ?: "m"),
                        wallThickness = inputs["wallThickness"]?.value?.toString() ?: "",
                        wallThicknessUnit = DimensionUnit.fromSymbol(inputs["wallThickness"]?.unit ?: "m"),
                        brickLength = inputs["brickLength"]?.value?.toString() ?: "",
                        brickLengthUnit = DimensionUnit.fromSymbol(inputs["brickLength"]?.unit ?: "cm"),
                        brickWidth = inputs["brickWidth"]?.value?.toString() ?: "",
                        brickWidthUnit = DimensionUnit.fromSymbol(inputs["brickWidth"]?.unit ?: "cm"),
                        brickHeight = inputs["brickHeight"]?.value?.toString() ?: "",
                        brickHeightUnit = DimensionUnit.fromSymbol(inputs["brickHeight"]?.unit ?: "cm"),
                        mortarJoint = inputs["mortarJoint"]?.value?.toString() ?: "",
                        mortarJointUnit = DimensionUnit.fromSymbol(inputs["mortarJoint"]?.unit ?: "cm"),
                        wastagePercentage = inputs["wastage"]?.value?.toString() ?: "5",
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
        isWallLengthTouched = true
        isWallHeightTouched = true
        isWallThicknessTouched = true
        isBrickLengthTouched = true
        isBrickWidthTouched = true
        isBrickHeightTouched = true
        isMortarJointTouched = true
        isWastageTouched = true
    }

    // Input handlers
    fun onWallLengthChange(v: String) { isWallLengthTouched = true; _uiState.update { it.copy(wallLength = v) }; validateInputs() }
    fun onWallLengthUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.wallLength, state.wallLengthUnit, u)
            state.copy(wallLength = newValue, wallLengthUnit = u)
        }
        validateInputs()
    }
    fun onWallHeightChange(v: String) { isWallHeightTouched = true; _uiState.update { it.copy(wallHeight = v) }; validateInputs() }
    fun onWallHeightUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.wallHeight, state.wallHeightUnit, u)
            state.copy(wallHeight = newValue, wallHeightUnit = u)
        }
        validateInputs()
    }
    fun onWallThicknessChange(v: String) { isWallThicknessTouched = true; _uiState.update { it.copy(wallThickness = v) }; validateInputs() }
    fun onWallThicknessUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.wallThickness, state.wallThicknessUnit, u)
            state.copy(wallThickness = newValue, wallThicknessUnit = u)
        }
        validateInputs()
    }

    fun onBrickLengthChange(v: String) { isBrickLengthTouched = true; _uiState.update { it.copy(brickLength = v) }; validateInputs() }
    fun onBrickLengthUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.brickLength, state.brickLengthUnit, u)
            state.copy(brickLength = newValue, brickLengthUnit = u)
        }
        validateInputs()
    }
    fun onBrickWidthChange(v: String) { isBrickWidthTouched = true; _uiState.update { it.copy(brickWidth = v) }; validateInputs() }
    fun onBrickWidthUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.brickWidth, state.brickWidthUnit, u)
            state.copy(brickWidth = newValue, brickWidthUnit = u)
        }
        validateInputs()
    }
    fun onBrickHeightChange(v: String) { isBrickHeightTouched = true; _uiState.update { it.copy(brickHeight = v) }; validateInputs() }
    fun onBrickHeightUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.brickHeight, state.brickHeightUnit, u)
            state.copy(brickHeight = newValue, brickHeightUnit = u)
        }
        validateInputs()
    }

    fun onMortarJointChange(v: String) { isMortarJointTouched = true; _uiState.update { it.copy(mortarJoint = v) }; validateInputs() }
    fun onMortarJointUnitChange(u: DimensionUnit) {
        _uiState.update { state ->
            val newValue = CalculatorViewModelUtils.convertValue(state.mortarJoint, state.mortarJointUnit, u)
            state.copy(mortarJoint = newValue, mortarJointUnit = u)
        }
        validateInputs()
    }
    fun onWastageChange(v: String) { isWastageTouched = true; _uiState.update { it.copy(wastagePercentage = v) }; validateInputs() }

    private fun validateInputs() {
        val s = _uiState.value
        
        val wl = s.wallLength.toDoubleOrNull()
        val wh = s.wallHeight.toDoubleOrNull()
        val wt = s.wallThickness.toDoubleOrNull()
        val bl = s.brickLength.toDoubleOrNull()
        val bw = s.brickWidth.toDoubleOrNull()
        val bh = s.brickHeight.toDoubleOrNull()
        val mj = s.mortarJoint.toDoubleOrNull()
        val waste = s.wastagePercentage.toDoubleOrNull()

        val wlErr = validateNumeric(s.wallLength, isWallLengthTouched, wl)
        val whErr = validateNumeric(s.wallHeight, isWallHeightTouched, wh)
        val wtErr = validateNumeric(s.wallThickness, isWallThicknessTouched, wt)
        val blErr = validateNumeric(s.brickLength, isBrickLengthTouched, bl)
        val bwErr = validateNumeric(s.brickWidth, isBrickWidthTouched, bw)
        val bhErr = validateNumeric(s.brickHeight, isBrickHeightTouched, bh)
        val mjErr = validateNumeric(s.mortarJoint, isMortarJointTouched, mj, allowZero = true)
        val wasteErr = validateNumeric(s.wastagePercentage, isWastageTouched, waste, allowZero = true)

        val isValid = wlErr == null && whErr == null && wtErr == null && 
                      blErr == null && bwErr == null && bhErr == null && 
                      mjErr == null && wasteErr == null &&
                      wl != null && wh != null && wt != null && 
                      bl != null && bw != null && bh != null && 
                      mj != null && waste != null

        val result = if (isValid) {
            calculator.calculate(
                wl!!, s.wallLengthUnit,
                wh!!, s.wallHeightUnit,
                wt!!, s.wallThicknessUnit,
                bl!!, s.brickLengthUnit,
                bw!!, s.brickWidthUnit,
                bh!!, s.brickHeightUnit,
                mj!!, s.mortarJointUnit,
                waste!!
            )
        } else null

        _uiState.update {
            it.copy(
                wallLengthError = wlErr, wallHeightError = whErr, wallThicknessError = wtErr,
                brickLengthError = blErr, brickWidthError = bwErr, brickHeightError = bhErr,
                mortarJointError = mjErr, wastageError = wasteErr,
                isCalculateEnabled = isValid,
                totalBricks = result?.totalBricks,
                wallVolume = result?.wallVolume
            )
        }
    }

    private fun validateNumeric(value: String, touched: Boolean, num: Double?, allowZero: Boolean = false): String? {
        if (!touched) return null
        if (value.isBlank()) return "Required"
        if (num == null) return "Invalid number"
        if (!allowZero && num <= 0) return "Must be > 0"
        if (allowZero && num < 0) return "Must be >= 0"
        return null
    }

    fun saveCalculation() {
        val s = _uiState.value
        val project = s.selectedProject ?: return
        val total = s.totalBricks ?: return

        viewModelScope.launch {
            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                type = CalculationType.BRICK,
                displayName = "Brick Count",
                formulaVersion = FORMULA_VERSION,
                inputs = listOf(
                    CalculationInput(key = "wallLength", value = s.wallLength.toDouble(), unit = s.wallLengthUnit.symbol),
                    CalculationInput(key = "wallHeight", value = s.wallHeight.toDouble(), unit = s.wallHeightUnit.symbol),
                    CalculationInput(key = "wallThickness", value = s.wallThickness.toDouble(), unit = s.wallThicknessUnit.symbol),
                    CalculationInput(key = "brickLength", value = s.brickLength.toDouble(), unit = s.brickLengthUnit.symbol),
                    CalculationInput(key = "brickWidth", value = s.brickWidth.toDouble(), unit = s.brickWidthUnit.symbol),
                    CalculationInput(key = "brickHeight", value = s.brickHeight.toDouble(), unit = s.brickHeightUnit.symbol),
                    CalculationInput(key = "mortarJoint", value = s.mortarJoint.toDouble(), unit = s.mortarJointUnit.symbol),
                    CalculationInput(key = "wastage", value = s.wastagePercentage.toDouble(), unit = "%")
                ),
                resultVal = total.toDouble(),
                resultUnit = "bricks",
                timestamp = System.currentTimeMillis()
            )
            engineeringRepository.insertCalculation(calculation)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
