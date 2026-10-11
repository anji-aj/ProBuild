package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.UnitConverter
import kotlin.math.ceil

class BrickCalculator {

    data class CalculationResult(
        val totalBricks: Int,
        val wallVolume: Double,
        val brickWithMortarVolume: Double
    )

    fun calculate(
        wallLength: Double,
        wallLengthUnit: DimensionUnit,
        wallHeight: Double,
        wallHeightUnit: DimensionUnit,
        wallThickness: Double,
        wallThicknessUnit: DimensionUnit,
        brickLength: Double,
        brickLengthUnit: DimensionUnit,
        brickWidth: Double,
        brickWidthUnit: DimensionUnit,
        brickHeight: Double,
        brickHeightUnit: DimensionUnit,
        mortarJoint: Double,
        mortarJointUnit: DimensionUnit,
        wastagePercentage: Double
    ): CalculationResult {
        // Convert all to meters
        val wl = UnitConverter.toMeters(wallLength, wallLengthUnit)
        val wh = UnitConverter.toMeters(wallHeight, wallHeightUnit)
        val wt = UnitConverter.toMeters(wallThickness, wallThicknessUnit)

        val bl = UnitConverter.toMeters(brickLength, brickLengthUnit)
        val bw = UnitConverter.toMeters(brickWidth, brickWidthUnit)
        val bh = UnitConverter.toMeters(brickHeight, brickHeightUnit)
        val mj = UnitConverter.toMeters(mortarJoint, mortarJointUnit)

        // 1. Wall Volume
        val wallVolume = wl * wh * wt

        // 2. Brick + Mortar Volume
        val brickWithMortarVolume = (bl + mj) * (bw + mj) * (bh + mj)

        // 3. Base Brick Count
        val baseCount = if (brickWithMortarVolume > 0) wallVolume / brickWithMortarVolume else 0.0

        // 4. Final Brick Count with Wastage
        val totalBricks = ceil(baseCount * (1 + wastagePercentage / 100.0)).toInt()

        return CalculationResult(
            totalBricks = totalBricks,
            wallVolume = wallVolume,
            brickWithMortarVolume = brickWithMortarVolume
        )
    }
}
