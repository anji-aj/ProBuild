package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.UnitConverter

class ConcreteVolumeCalculator {

    data class CalculationResult(
        val cubicMeters: Double,
        val cubicFeet: Double
    )

    fun calculate(
        length: Double,
        lengthUnit: DimensionUnit,
        width: Double,
        widthUnit: DimensionUnit,
        depth: Double,
        depthUnit: DimensionUnit
    ): CalculationResult {
        val l = UnitConverter.toMeters(length, lengthUnit)
        val w = UnitConverter.toMeters(width, widthUnit)
        val d = UnitConverter.toMeters(depth, depthUnit)

        val m3 = l * w * d
        val ft3 = UnitConverter.cubicMetersToCubicFeet(m3)

        return CalculationResult(m3, ft3)
    }
}
