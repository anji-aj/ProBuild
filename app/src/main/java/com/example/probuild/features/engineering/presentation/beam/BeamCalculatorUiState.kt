package com.example.probuild.features.engineering.presentation.beam

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.model.Project

data class BeamCalculatorUiState(
    // Beam Dimensions
    val length: String = "",
    val lengthUnit: DimensionUnit = DimensionUnit.METER,
    val width: String = "",
    val widthUnit: DimensionUnit = DimensionUnit.CENTIMETER,
    val depth: String = "",
    val depthUnit: DimensionUnit = DimensionUnit.CENTIMETER,

    // Reinforcement
    val mainBarCount: String = "",
    val mainBarDiameter: String = "",
    val mainBarDiameterUnit: DimensionUnit = DimensionUnit.MILLIMETER,
    val stirrupDiameter: String = "",
    val stirrupDiameterUnit: DimensionUnit = DimensionUnit.MILLIMETER,
    val stirrupSpacing: String = "",
    val stirrupSpacingUnit: DimensionUnit = DimensionUnit.MILLIMETER,

    // Errors
    val lengthError: String? = null,
    val widthError: String? = null,
    val depthError: String? = null,
    val mainBarCountError: String? = null,
    val mainBarDiameterError: String? = null,
    val stirrupDiameterError: String? = null,
    val stirrupSpacingError: String? = null,

    // Results
    val concreteVolumeM3: Double? = null,
    val mainSteelWeightKg: Double? = null,
    val stirrupWeightKg: Double? = null,
    val totalSteelWeightKg: Double? = null,
    val stirrupCount: Int? = null,

    // Metadata
    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
