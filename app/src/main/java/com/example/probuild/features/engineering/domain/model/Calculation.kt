package com.example.probuild.features.engineering.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Calculation(
    val id: String,
    val projectId: String,
    val type: CalculationType,
    val displayName: String,
    val formulaVersion: String,
    val inputs: List<CalculationInput>,
    val resultVal: Double,
    val resultUnit: String,
    val timestamp: Long,
    val isFavorite: Boolean = false
)

data class CalculationInput(
    val id: String = "",
    val key: String,
    val value: Double,
    val unit: String
)

enum class CalculationType(
    val category: CalculationCategory,
    val title: String,
    val description: String
) {
    CONCRETE(CalculationCategory.STRUCTURAL, "Concrete", "Calculate volume and mix ratios"),
    BEAM(CalculationCategory.STRUCTURAL, "Beam", "Reinforcement and load analysis"),
    COLUMN(CalculationCategory.STRUCTURAL, "Column", "Capacity and detail checks"),
    SLAB(CalculationCategory.STRUCTURAL, "Slab", "Area and thickness quantities"),
    FOUNDATION(CalculationCategory.STRUCTURAL, "Foundation", "Excavation and base requirements"),
    
    BRICK(CalculationCategory.QUANTITY, "Brick", "Estimate total bricks and mortar"),
    STEEL(CalculationCategory.QUANTITY, "Steel", "Weight and bar schedules"),
    AREA(CalculationCategory.QUANTITY, "Area", "Quick surface measurements"),
    VOLUME(CalculationCategory.QUANTITY, "Volume", "Bulk material estimations"),
    
    UNIT_CONVERTER(CalculationCategory.UTILITIES, "Unit Converter", "Convert common engineering units");

    val icon: ImageVector get() = when (this) {
        CONCRETE -> Icons.Default.Layers
        BEAM -> Icons.Default.ViewStream
        COLUMN -> Icons.Default.VerticalAlignTop
        SLAB -> Icons.Default.Texture
        FOUNDATION -> Icons.Default.Foundation
        BRICK -> Icons.Default.Dashboard
        STEEL -> Icons.Default.Architecture
        AREA -> Icons.Default.SquareFoot
        VOLUME -> Icons.Default.ViewInAr
        UNIT_CONVERTER -> Icons.Default.SyncAlt
    }
}

enum class CalculationCategory {
    STRUCTURAL, QUANTITY, UTILITIES
}
