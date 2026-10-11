package com.example.probuild.features.engineering.presentation.area

import com.example.probuild.features.engineering.domain.logic.AreaCalculator
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.model.Project

data class AreaCalculatorUiState(
    val selectedShape: AreaCalculator.Shape = AreaCalculator.Shape.RECTANGLE,
    
    // Generic Dimension Inputs (used differently per shape)
    val dim1: String = "",
    val dim1Unit: DimensionUnit = DimensionUnit.METER,
    val dim2: String = "",
    val dim2Unit: DimensionUnit = DimensionUnit.METER,

    val dim1Error: String? = null,
    val dim2Error: String? = null,

    val resultM2: Double? = null,
    val resultFt2: Double? = null,

    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
