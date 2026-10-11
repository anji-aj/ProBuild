package com.example.probuild.features.engineering.presentation.unit_converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.logic.EngineeringUnitConverter
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationInput
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.engineering.util.EngineeringConstants.FORMULA_VERSION
import com.example.probuild.features.projects.domain.ProjectsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class UnitConverterViewModel(
    private val engineeringRepository: EngineeringRepository,
    private val projectsRepository: ProjectsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState = _uiState.asStateFlow()

    private val converter = EngineeringUnitConverter()

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
            if (calculation != null && calculation.type == CalculationType.UNIT_CONVERTER) {
                val inputs = calculation.inputs.associateBy { it.key }
                val categoryStr = inputs["category"]?.unit ?: EngineeringUnitConverter.Category.LENGTH.name
                val category = EngineeringUnitConverter.Category.valueOf(categoryStr)
                
                _uiState.update { state ->
                    state.copy(
                        selectedCategory = category,
                        inputValue = inputs["value"]?.value?.toString() ?: "",
                        fromUnitSymbol = inputs["value"]?.unit ?: "m",
                        toUnitSymbol = calculation.resultUnit,
                        isLoading = false
                    )
                }
                validateAndConvert()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onCategorySelected(category: EngineeringUnitConverter.Category) {
        val units = converter.unitsByCategory[category] ?: emptyList()
        val from = units.firstOrNull()?.symbol ?: ""
        val to = units.getOrNull(1)?.symbol ?: from
        
        _uiState.update { 
            it.copy(
                selectedCategory = category,
                fromUnitSymbol = from,
                toUnitSymbol = to,
                inputValue = "" // Clear input on category change
            )
        }
        validateAndConvert()
    }

    fun onInputValueChange(value: String) {
        _uiState.update { it.copy(inputValue = value) }
        validateAndConvert()
    }

    fun onFromUnitChange(symbol: String) {
        _uiState.update { it.copy(fromUnitSymbol = symbol) }
        validateAndConvert()
    }

    fun onToUnitChange(symbol: String) {
        _uiState.update { it.copy(toUnitSymbol = symbol) }
        validateAndConvert()
    }

    fun swapUnits() {
        _uiState.update { 
            it.copy(
                fromUnitSymbol = it.toUnitSymbol,
                toUnitSymbol = it.fromUnitSymbol
            )
        }
        validateAndConvert()
    }

    private fun validateAndConvert() {
        val s = _uiState.value
        val value = s.inputValue.toDoubleOrNull()
        
        val error = when {
            s.inputValue.isBlank() -> null // Not an error yet
            value == null -> "Invalid number"
            value.isNaN() -> "Invalid value (NaN)"
            value.isInfinite() -> "Value too large (Infinity)"
            else -> null
        }

        val result = if (error == null && value != null) {
            converter.convert(value, s.fromUnitSymbol, s.toUnitSymbol, s.selectedCategory)
        } else null

        _uiState.update { 
            it.copy(
                error = error,
                convertedResult = result,
                isCalculateEnabled = error == null && value != null
            )
        }
    }

    fun saveCalculation() {
        val s = _uiState.value
        val project = s.selectedProject ?: return
        val value = s.inputValue.toDoubleOrNull() ?: return
        val result = s.convertedResult ?: return

        viewModelScope.launch {
            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                type = CalculationType.UNIT_CONVERTER,
                displayName = "Unit Conversion",
                formulaVersion = FORMULA_VERSION,
                inputs = listOf(
                    CalculationInput(key = "category", value = 0.0, unit = s.selectedCategory.name),
                    CalculationInput(key = "value", value = value, unit = s.fromUnitSymbol)
                ),
                resultVal = result,
                resultUnit = s.toUnitSymbol,
                timestamp = System.currentTimeMillis()
            )
            engineeringRepository.insertCalculation(calculation)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
