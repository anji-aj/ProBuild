package com.example.probuild.features.engineering.presentation.concrete

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.model.Project

data class ConcreteCalculatorUiState(
    val length: String = "",
    val lengthUnit: DimensionUnit = DimensionUnit.METER,
    val width: String = "",
    val widthUnit: DimensionUnit = DimensionUnit.METER,
    val depth: String = "",
    val depthUnit: DimensionUnit = DimensionUnit.METER,
    
    val lengthError: String? = null,
    val widthError: String? = null,
    val depthError: String? = null,
    
    val resultM3: Double? = null,
    val resultFt3: Double? = null,
    
    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
