package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.UnitConverter
import kotlin.math.PI

class AreaCalculator {

    enum class Shape {
        RECTANGLE, SQUARE, TRIANGLE, CIRCLE
    }

    data class CalculationResult(
        val areaM2: Double,
        val areaFt2: Double
    )

    fun calculateRectangle(
        length: Double, lengthUnit: DimensionUnit,
        width: Double, widthUnit: DimensionUnit
    ): CalculationResult {
        val l = UnitConverter.toMeters(length, lengthUnit)
        val w = UnitConverter.toMeters(width, widthUnit)
        val area = l * w
        return CalculationResult(area, UnitConverter.squareMetersToSquareFeet(area))
    }

    fun calculateSquare(
        side: Double, sideUnit: DimensionUnit
    ): CalculationResult {
        val s = UnitConverter.toMeters(side, sideUnit)
        val area = s * s
        return CalculationResult(area, UnitConverter.squareMetersToSquareFeet(area))
    }

    fun calculateTriangle(
        base: Double, baseUnit: DimensionUnit,
        height: Double, heightUnit: DimensionUnit
    ): CalculationResult {
        val b = UnitConverter.toMeters(base, baseUnit)
        val h = UnitConverter.toMeters(height, heightUnit)
        val area = 0.5 * b * h
        return CalculationResult(area, UnitConverter.squareMetersToSquareFeet(area))
    }

    fun calculateCircle(
        radius: Double, radiusUnit: DimensionUnit
    ): CalculationResult {
        val r = UnitConverter.toMeters(radius, radiusUnit)
        val area = PI * r * r
        return CalculationResult(area, UnitConverter.squareMetersToSquareFeet(area))
    }
}
