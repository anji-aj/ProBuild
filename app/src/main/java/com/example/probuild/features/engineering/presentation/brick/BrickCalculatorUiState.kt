package com.example.probuild.features.engineering.presentation.brick

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.projects.domain.model.Project

data class BrickCalculatorUiState(
    // Wall Inputs
    val wallLength: String = "",
    val wallLengthUnit: DimensionUnit = DimensionUnit.METER,
    val wallHeight: String = "",
    val wallHeightUnit: DimensionUnit = DimensionUnit.METER,
    val wallThickness: String = "",
    val wallThicknessUnit: DimensionUnit = DimensionUnit.METER,

    // Brick Inputs
    val brickLength: String = "",
    val brickLengthUnit: DimensionUnit = DimensionUnit.CENTIMETER,
    val brickWidth: String = "",
    val brickWidthUnit: DimensionUnit = DimensionUnit.CENTIMETER,
    val brickHeight: String = "",
    val brickHeightUnit: DimensionUnit = DimensionUnit.CENTIMETER,

    // Mortar & Wastage
    val mortarJoint: String = "",
    val mortarJointUnit: DimensionUnit = DimensionUnit.CENTIMETER,
    val wastagePercentage: String = "5",

    // Errors
    val wallLengthError: String? = null,
    val wallHeightError: String? = null,
    val wallThicknessError: String? = null,
    val brickLengthError: String? = null,
    val brickWidthError: String? = null,
    val brickHeightError: String? = null,
    val mortarJointError: String? = null,
    val wastageError: String? = null,

    // Results
    val totalBricks: Int? = null,
    val wallVolume: Double? = null,

    // Metadata
    val selectedProject: Project? = null,
    val isCalculateEnabled: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
