package com.example.probuild.features.engineering.presentation.foundation

import com.example.probuild.features.engineering.domain.logic.FoundationCalculator
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.model.Project

data class FoundationCalculatorUiState(
    val selectedType: FoundationCalculator.FoundationType = FoundationCalculator.FoundationType.RECTANGULAR,
    
    // Dimensions
    val length: String = "",
    val lengthUnit: DimensionUnit = DimensionUnit.METER,
    val width: String = "",
    val widthUnit: DimensionUnit = DimensionUnit.METER,
    val thickness: String = "",
    val thicknessUnit: DimensionUnit = DimensionUnit.CENTIMETER,

    // Reinforcement
    val mainDiameter: String = "",
    val mainDiameterUnit: DimensionUnit = DimensionUnit.MILLIMETER,
    val mainSpacing: String = "",
    val mainSpacingUnit: DimensionUnit = DimensionUnit.CENTIMETER,
    val distDiameter: String = "",
    val distDiameterUnit: DimensionUnit = DimensionUnit.MILLIMETER,
    val distSpacing: String = "",
    val distSpacingUnit: DimensionUnit = DimensionUnit.CENTIMETER,

    // Errors
    val lengthError: String? = null,
    val widthError: String? = null,
    val thicknessError: String? = null,
    val mainDiameterError: String? = null,
    val mainSpacingError: String? = null,
    val distDiameterError: String? = null,
    val distSpacingError: String? = null,

    // Results
    val concreteVolumeM3: Double? = null,
    val mainSteelWeightKg: Double? = null,
    val distributionSteelWeightKg: Double? = null,
    val totalSteelWeightKg: Double? = null,
    val mainBarCount: Int? = null,
    val distributionBarCount: Int? = null,

    // Metadata
    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
