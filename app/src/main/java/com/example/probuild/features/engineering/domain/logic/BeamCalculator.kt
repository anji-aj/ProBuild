package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.EngineeringConstants.STEEL_DENSITY
import com.example.probuild.features.engineering.util.UnitConverter
import kotlin.math.PI
import kotlin.math.ceil

class BeamCalculator {

    data class CalculationResult(
        val concreteVolumeM3: Double,
        val mainSteelWeightKg: Double,
        val stirrupWeightKg: Double,
        val totalSteelWeightKg: Double,
        val stirrupCount: Int
    )

    fun calculate(
        length: Double, lengthUnit: DimensionUnit,
        width: Double, widthUnit: DimensionUnit,
        depth: Double, depthUnit: DimensionUnit,
        mainBarCount: Int,
        mainBarDiameter: Double, mainBarDiameterUnit: DimensionUnit,
        stirrupDiameter: Double, stirrupDiameterUnit: DimensionUnit,
        stirrupSpacing: Double, stirrupSpacingUnit: DimensionUnit
    ): CalculationResult {
        // 1. Convert Beam Dimensions to meters
        val lMeters = UnitConverter.toMeters(length, lengthUnit)
        val wMeters = UnitConverter.toMeters(width, widthUnit)
        val dMeters = UnitConverter.toMeters(depth, depthUnit)

        // 2. Concrete Volume
        val concreteVolume = lMeters * wMeters * dMeters

        // 3. Main Reinforcement Weight
        val mainDMeters = UnitConverter.toMeters(mainBarDiameter, mainBarDiameterUnit)
        val mainArea = PI * (mainDMeters * mainDMeters) / 4.0
        val mainWeight = mainBarCount * lMeters * mainArea * STEEL_DENSITY

        // 4. Stirrup Count
        val spacingMeters = UnitConverter.toMeters(stirrupSpacing, stirrupSpacingUnit)
        val stirrupCount = if (spacingMeters > 0) {
            // Using a tiny epsilon to handle floating point precision for exact divisions
            ceil((lMeters / spacingMeters) - 1e-9).toInt() + 1
        } else 0

        // 5. Stirrup Length (Approximate perimeter = 2 * (Width + Depth))
        val stirrupLengthMeters = 2.0 * (wMeters + dMeters)

        // 6. Stirrup Weight
        val stirrupDMeters = UnitConverter.toMeters(stirrupDiameter, stirrupDiameterUnit)
        val stirrupArea = PI * (stirrupDMeters * stirrupDMeters) / 4.0
        val stirrupWeight = stirrupCount * stirrupLengthMeters * stirrupArea * STEEL_DENSITY

        return CalculationResult(
            concreteVolumeM3 = concreteVolume,
            mainSteelWeightKg = mainWeight,
            stirrupWeightKg = stirrupWeight,
            totalSteelWeightKg = mainWeight + stirrupWeight,
            stirrupCount = stirrupCount
        )
    }
}
