package com.example.probuild.features.engineering.presentation.volume

import com.example.probuild.features.engineering.domain.logic.VolumeCalculator
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.model.Project

data class VolumeCalculatorUiState(
    val selectedShape: VolumeCalculator.Shape = VolumeCalculator.Shape.RECTANGULAR_PRISM,
    
    // Dimension Inputs
    val dim1: String = "",
    val dim1Unit: DimensionUnit = DimensionUnit.METER,
    val dim2: String = "",
    val dim2Unit: DimensionUnit = DimensionUnit.METER,
    val dim3: String = "",
    val dim3Unit: DimensionUnit = DimensionUnit.METER,

    val dim1Error: String? = null,
    val dim2Error: String? = null,
    val dim3Error: String? = null,

    val resultM3: Double? = null,
    val resultFt3: Double? = null,

    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
