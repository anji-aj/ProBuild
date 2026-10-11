package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class SteelWeightCalculatorTest {

    private val calculator = SteelWeightCalculator()

    @Test
    fun `calculate - 12mm bar 1m long check`() {
        // Diameter 12mm
        // Length 1m
        // 1 bar
        val result = calculator.calculate(
            diameter = 12.0,
            diameterUnit = DimensionUnit.MILLIMETER,
            length = 1.0,
            lengthUnit = DimensionUnit.METER,
            numberOfBars = 1
        )

        // Expected weight ~0.888 kg
        assertEquals(0.8878, result.weightKg, 0.001)
    }

    @Test
    fun `calculate - mixed units and multiple bars`() {
        // Diameter 1 inch = 2.54 cm
        // Length 10 feet = 120 inch
        // 5 bars
        val result = calculator.calculate(
            diameter = 1.0,
            diameterUnit = DimensionUnit.INCH,
            length = 10.0,
            lengthUnit = DimensionUnit.FEET,
            numberOfBars = 5
        )

        // d = 0.0254 m
        // l = 10 * 0.3048 = 3.048 m
        // A = PI * (0.0254^2) / 4 = 0.0005067 m2
        // V = A * 3.048 * 5 = 0.007722 m3
        // W = V * 7850 = 60.618 kg
        assertEquals(60.618, result.weightKg, 0.01)
    }
}
