package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.UnitConverter
import kotlin.math.PI

class VolumeCalculator {

    enum class Shape {
        RECTANGULAR_PRISM, CUBE, CYLINDER, SPHERE
    }

    data class CalculationResult(
        val volumeM3: Double,
        val volumeFt3: Double
    )

    fun calculateRectangularPrism(
        length: Double, lengthUnit: DimensionUnit,
        width: Double, widthUnit: DimensionUnit,
        height: Double, heightUnit: DimensionUnit
    ): CalculationResult {
        val l = UnitConverter.toMeters(length, lengthUnit)
        val w = UnitConverter.toMeters(width, widthUnit)
        val h = UnitConverter.toMeters(height, heightUnit)
        val volume = l * w * h
        return CalculationResult(volume, UnitConverter.cubicMetersToCubicFeet(volume))
    }

    fun calculateCube(
        side: Double, sideUnit: DimensionUnit
    ): CalculationResult {
        val s = UnitConverter.toMeters(side, sideUnit)
        val volume = s * s * s
        return CalculationResult(volume, UnitConverter.cubicMetersToCubicFeet(volume))
    }

    fun calculateCylinder(
        radius: Double, radiusUnit: DimensionUnit,
        height: Double, heightUnit: DimensionUnit
    ): CalculationResult {
        val r = UnitConverter.toMeters(radius, radiusUnit)
        val h = UnitConverter.toMeters(height, heightUnit)
        val volume = PI * r * r * h
        return CalculationResult(volume, UnitConverter.cubicMetersToCubicFeet(volume))
    }

    fun calculateSphere(
        radius: Double, radiusUnit: DimensionUnit
    ): CalculationResult {
        val r = UnitConverter.toMeters(radius, radiusUnit)
        val volume = (4.0 / 3.0) * PI * r * r * r
        return CalculationResult(volume, UnitConverter.cubicMetersToCubicFeet(volume))
    }
}
