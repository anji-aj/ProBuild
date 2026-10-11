package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class AreaCalculatorTest {

    private val calculator = AreaCalculator()

    @Test
    fun `calculateRectangle - basic metric`() {
        val result = calculator.calculateRectangle(
            length = 5.0, lengthUnit = DimensionUnit.METER,
            width = 3.0, widthUnit = DimensionUnit.METER
        )
        assertEquals(15.0, result.areaM2, 0.0001)
        assertEquals(161.4586, result.areaFt2, 0.001)
    }

    @Test
    fun `calculateSquare - mixed units`() {
        // 10 feet = 3.048 meters
        val result = calculator.calculateSquare(
            side = 10.0, sideUnit = DimensionUnit.FEET
        )
        val expectedM2 = 3.048 * 3.048
        assertEquals(expectedM2, result.areaM2, 0.0001)
    }

    @Test
    fun `calculateTriangle - mixed units`() {
        // base 100 cm = 1m
        // height 2m
        val result = calculator.calculateTriangle(
            base = 100.0, baseUnit = DimensionUnit.CENTIMETER,
            height = 2.0, heightUnit = DimensionUnit.METER
        )
        // Area = 0.5 * 1 * 2 = 1.0 m2
        assertEquals(1.0, result.areaM2, 0.0001)
    }

    @Test
    fun `calculateCircle - imperial`() {
        // radius 12 inch = 1 ft = 0.3048 m
        val result = calculator.calculateCircle(
            radius = 12.0, radiusUnit = DimensionUnit.INCH
        )
        val expectedM2 = PI * 0.3048 * 0.3048
        assertEquals(expectedM2, result.areaM2, 0.0001)
    }
}
