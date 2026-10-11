package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.EngineeringConstants.STEEL_DENSITY
import com.example.probuild.features.engineering.util.UnitConverter
import kotlin.math.PI
import kotlin.math.ceil

class ColumnCalculator {

    data class CalculationResult(
        val concreteVolumeM3: Double,
        val mainSteelWeightKg: Double,
        val tieWeightKg: Double,
        val totalSteelWeightKg: Double,
        val tieCount: Int
    )

    fun calculate(
        height: Double, heightUnit: DimensionUnit,
        width: Double, widthUnit: DimensionUnit,
        depth: Double, depthUnit: DimensionUnit,
        mainBarCount: Int,
        mainBarDiameter: Double, mainBarDiameterUnit: DimensionUnit,
        tieDiameter: Double, tieDiameterUnit: DimensionUnit,
        tieSpacing: Double, tieSpacingUnit: DimensionUnit
    ): CalculationResult {
        // 1. Convert Column Dimensions to meters
        val hMeters = UnitConverter.toMeters(height, heightUnit)
        val wMeters = UnitConverter.toMeters(width, widthUnit)
        val dMeters = UnitConverter.toMeters(depth, depthUnit)

        // 2. Concrete Volume
        val concreteVolume = hMeters * wMeters * dMeters

        // 3. Main Reinforcement Weight
        val mainDMeters = UnitConverter.toMeters(mainBarDiameter, mainBarDiameterUnit)
        val mainArea = PI * (mainDMeters * mainDMeters) / 4.0
        val mainWeight = mainBarCount * hMeters * mainArea * STEEL_DENSITY

        // 4. Tie Count
        val spacingMeters = UnitConverter.toMeters(tieSpacing, tieSpacingUnit)
        val tieCount = if (spacingMeters > 0) {
            // Using epsilon handling for floating point division as established in Beam Calculator
            ceil((hMeters / spacingMeters) - 1e-9).toInt() + 1
        } else 0

        // 5. Tie Length (Perimeter = 2 * (Width + Depth))
        val tieLengthMeters = 2.0 * (wMeters + dMeters)

        // 6. Tie Weight
        val tieDMeters = UnitConverter.toMeters(tieDiameter, tieDiameterUnit)
        val tieArea = PI * (tieDMeters * tieDMeters) / 4.0
        val tieWeight = tieCount * tieLengthMeters * tieArea * STEEL_DENSITY

        return CalculationResult(
            concreteVolumeM3 = concreteVolume,
            mainSteelWeightKg = mainWeight,
            tieWeightKg = tieWeight,
            totalSteelWeightKg = mainWeight + tieWeight,
            tieCount = tieCount
        )
    }
}
