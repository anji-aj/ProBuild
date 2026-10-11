package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class SlabCalculatorTest {

    private val calculator = SlabCalculator()

    @Test
    fun `calculate - basic metric scenario`() {
        // Slab: 5m(L) x 3m(W) x 0.15m(T) = 2.25 m3
        // Main Bars: 12mm diameter, 150mm spacing
        // Distribution Bars: 8mm diameter, 200mm spacing
        
        val result = calculator.calculate(
            length = 5.0, lengthUnit = DimensionUnit.METER,
            width = 3.0, widthUnit = DimensionUnit.METER,
            thickness = 15.0, thicknessUnit = DimensionUnit.CENTIMETER,
            mainDiameter = 12.0, mainDiameterUnit = DimensionUnit.MILLIMETER,
            mainSpacing = 150.0, mainSpacingUnit = DimensionUnit.MILLIMETER,
            distDiameter = 8.0, distDiameterUnit = DimensionUnit.MILLIMETER,
            distSpacing = 200.0, distSpacingUnit = DimensionUnit.MILLIMETER
        )

        // 1. Concrete Volume = 5 * 3 * 0.15 = 2.25 m3
        assertEquals(2.25, result.concreteVolumeM3, 0.0001)

        // 2. Main Bars (across width 3m)
        // Count = CEIL(3 / 0.15) + 1 = 20 + 1 = 21
        // Length = 5m per bar -> Total = 5 * 21 = 105m
        // d = 0.012m -> Area = PI * 0.012^2 / 4 = 0.0001131 m2
        // Weight = 105 * 0.0001131 * 7850 = 93.22 kg
        assertEquals(21, result.mainBarCount)
        val expectedMainArea = PI * 0.012 * 0.012 / 4.0
        val expectedMainWeight = 21 * 5.0 * expectedMainArea * 7850.0
        assertEquals(expectedMainWeight, result.mainSteelWeightKg, 0.01)

        // 3. Distribution Bars (across length 5m)
        // Count = CEIL(5 / 0.20) + 1 = 25 + 1 = 26
        // Length = 3m per bar -> Total = 3 * 26 = 78m
        // d = 0.008m -> Area = PI * 0.008^2 / 4 = 0.00005026 m2
        // Weight = 78 * 0.00005026 * 7850 = 30.77 kg
        assertEquals(26, result.distributionBarCount)
        val expectedDistArea = PI * 0.008 * 0.008 / 4.0
        val expectedDistWeight = 26 * 3.0 * expectedDistArea * 7850.0
        assertEquals(expectedDistWeight, result.distributionSteelWeightKg, 0.01)
        
        assertEquals(expectedMainWeight + expectedDistWeight, result.totalSteelWeightKg, 0.01)
    }

    @Test
    fun `calculate - mixed units scenario`() {
        // 10ft x 10ft x 4in slab
        val result = calculator.calculate(
            length = 10.0, lengthUnit = DimensionUnit.FEET,
            width = 10.0, widthUnit = DimensionUnit.FEET,
            thickness = 4.0, thicknessUnit = DimensionUnit.INCH,
            mainDiameter = 0.5, mainDiameterUnit = DimensionUnit.INCH,
            mainSpacing = 6.0, mainSpacingUnit = DimensionUnit.INCH,
            distDiameter = 0.5, distDiameterUnit = DimensionUnit.INCH,
            distSpacing = 6.0, distSpacingUnit = DimensionUnit.INCH
        )
        
        // 10ft = 3.048m, 4in = 0.1016m
        // Vol = 3.048 * 3.048 * 0.1016 = 0.9439 m3
        assertEquals(0.9439, result.concreteVolumeM3, 0.001)
        
        // Count = CEIL(3.048 / 0.1524) + 1 = 20 + 1 = 21
        assertEquals(21, result.mainBarCount)
        assertEquals(21, result.distributionBarCount)
    }
}
