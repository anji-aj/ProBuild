package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.EngineeringConstants.STEEL_DENSITY
import com.example.probuild.features.engineering.util.UnitConverter
import kotlin.math.PI
import kotlin.math.ceil

class FoundationCalculator {

    enum class FoundationType {
        RECTANGULAR, SQUARE
    }

    data class CalculationResult(
        val concreteVolumeM3: Double,
        val mainSteelWeightKg: Double,
        val distributionSteelWeightKg: Double,
        val totalSteelWeightKg: Double,
        val mainBarCount: Int,
        val distributionBarCount: Int
    )

    fun calculate(
        type: FoundationType,
        length: Double, lengthUnit: DimensionUnit,
        width: Double, widthUnit: DimensionUnit,
        thickness: Double, thicknessUnit: DimensionUnit,
        mainDiameter: Double, mainDiameterUnit: DimensionUnit,
        mainSpacing: Double, mainSpacingUnit: DimensionUnit,
        distDiameter: Double, distDiameterUnit: DimensionUnit,
        distSpacing: Double, distSpacingUnit: DimensionUnit
    ): CalculationResult {
        // For Square footing, we expect length and width to be equal (passed by caller)
        val lMeters = UnitConverter.toMeters(length, lengthUnit)
        val wMeters = UnitConverter.toMeters(width, widthUnit)
        val tMeters = UnitConverter.toMeters(thickness, thicknessUnit)

        // 1. Concrete Volume
        val concreteVolume = lMeters * wMeters * tMeters

        // 2. Main Reinforcement (runs across width, spacing along width span?) 
        // Instructions: "Main bars run across the footing width: Count = CEILING(Width / MainSpacing) + 1"
        // "Main bar length = Length"
        val mainSpacingMeters = UnitConverter.toMeters(mainSpacing, mainSpacingUnit)
        val mainBarCount = if (mainSpacingMeters > 0) {
            ceil((wMeters / mainSpacingMeters) - 1e-9).toInt() + 1
        } else 0

        val mainDMeters = UnitConverter.toMeters(mainDiameter, mainDiameterUnit)
        val mainArea = PI * (mainDMeters * mainDMeters) / 4.0
        val mainTotalLength = lMeters * mainBarCount
        val mainWeight = mainTotalLength * mainArea * STEEL_DENSITY

        // 3. Distribution Reinforcement (runs across length, spacing along length span?)
        // Instructions: "Distribution bars run across the footing length: Count = CEILING(Length / DistributionSpacing) + 1"
        // "Distribution bar length = Width"
        val distSpacingMeters = UnitConverter.toMeters(distSpacing, distSpacingUnit)
        val distributionBarCount = if (distSpacingMeters > 0) {
            ceil((lMeters / distSpacingMeters) - 1e-9).toInt() + 1
        } else 0

        val distDMeters = UnitConverter.toMeters(distDiameter, distDiameterUnit)
        val distArea = PI * (distDMeters * distDMeters) / 4.0
        val distTotalLength = wMeters * distributionBarCount
        val distWeight = distTotalLength * distArea * STEEL_DENSITY

        return CalculationResult(
            concreteVolumeM3 = concreteVolume,
            mainSteelWeightKg = mainWeight,
            distributionSteelWeightKg = distWeight,
            totalSteelWeightKg = mainWeight + distWeight,
            mainBarCount = mainBarCount,
            distributionBarCount = distributionBarCount
        )
    }
}
