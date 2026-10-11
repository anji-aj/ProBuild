package com.example.probuild.features.engineering.presentation.steel

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.model.Project

data class SteelCalculatorUiState(
    val barDiameter: String = "",
    val diameterUnit: DimensionUnit = DimensionUnit.MILLIMETER,
    val barLength: String = "",
    val lengthUnit: DimensionUnit = DimensionUnit.METER,
    val numberOfBars: String = "1",

    val diameterError: String? = null,
    val lengthError: String? = null,
    val countError: String? = null,

    val resultWeightKg: Double? = null,
    val resultVolumeM3: Double? = null,
    val resultTotalBars: Int? = null,

    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
