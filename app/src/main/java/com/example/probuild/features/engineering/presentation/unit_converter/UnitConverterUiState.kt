package com.example.probuild.features.engineering.presentation.unit_converter

import com.example.probuild.features.engineering.domain.logic.EngineeringUnitConverter
import com.example.probuild.features.projects.domain.model.Project

data class UnitConverterUiState(
    val selectedCategory: EngineeringUnitConverter.Category = EngineeringUnitConverter.Category.LENGTH,
    val inputValue: String = "",
    val fromUnitSymbol: String = "m",
    val toUnitSymbol: String = "ft",
    
    val convertedResult: Double? = null,
    val error: String? = null,
    
    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
