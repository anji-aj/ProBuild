package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class FoundationCalculatorTest {

    private val calculator = FoundationCalculator()

    @Test
    fun `calculate - rectangular footing basic metric`() {
        // Footing: 2m(L) x 1.5m(W) x 0.4m(T) = 1.2 m3
        // Main Bars (across width): 12mm dia, 150mm spacing
        // Distribution Bars (across length): 10mm dia, 200mm spacing
        
        val result = calculator.calculate(
            type = FoundationCalculator.FoundationType.RECTANGULAR,
            length = 2.0, lengthUnit = DimensionUnit.METER,
            width = 1.5, widthUnit = DimensionUnit.METER,
            thickness = 40.0, thicknessUnit = DimensionUnit.CENTIMETER,
            mainDiameter = 12.0, mainDiameterUnit = DimensionUnit.MILLIMETER,
            mainSpacing = 150.0, mainSpacingUnit = DimensionUnit.MILLIMETER,
            distDiameter = 10.0, distDiameterUnit = DimensionUnit.MILLIMETER,
            distSpacing = 200.0, distSpacingUnit = DimensionUnit.MILLIMETER
        )

        // 1. Concrete Volume = 2 * 1.5 * 0.4 = 1.2 m3
        assertEquals(1.2, result.concreteVolumeM3, 0.0001)

        // 2. Main Bars (along width 1.5m)
        // Count = CEIL(1.5 / 0.15) + 1 = 10 + 1 = 11
        // Length = 2m per bar -> Total = 11 * 2 = 22m
        // d = 0.012m -> Area = PI * 0.012^2 / 4 = 0.0001131 m2
        // Weight = 22 * 0.0001131 * 7850 = 19.53 kg
        assertEquals(11, result.mainBarCount)
        val expectedMainArea = PI * 0.012 * 0.012 / 4.0
        val expectedMainWeight = 11 * 2.0 * expectedMainArea * 7850.0
        assertEquals(expectedMainWeight, result.mainSteelWeightKg, 0.01)

        // 3. Distribution Bars (along length 2m)
        // Count = CEIL(2 / 0.20) + 1 = 10 + 1 = 11
        // Length = 1.5m per bar -> Total = 11 * 1.5 = 16.5m
        // d = 0.010m -> Area = PI * 0.010^2 / 4 = 0.00007854 m2
        // Weight = 16.5 * 0.00007854 * 7850 = 10.17 kg
        assertEquals(11, result.distributionBarCount)
        val expectedDistArea = PI * 0.010 * 0.010 / 4.0
        val expectedDistWeight = 11 * 1.5 * expectedDistArea * 7850.0
        assertEquals(expectedDistWeight, result.distributionSteelWeightKg, 0.01)
        
        assertEquals(expectedMainWeight + expectedDistWeight, result.totalSteelWeightKg, 0.01)
    }

    @Test
    fun `calculate - square footing linked side`() {
        // Footing: 1.2m side, 0.3m thickness
        val result = calculator.calculate(
            type = FoundationCalculator.FoundationType.SQUARE,
            length = 1.2, lengthUnit = DimensionUnit.METER,
            width = 1.2, widthUnit = DimensionUnit.METER,
            thickness = 30.0, thicknessUnit = DimensionUnit.CENTIMETER,
            mainDiameter = 12.0, mainDiameterUnit = DimensionUnit.MILLIMETER,
            mainSpacing = 15.0, mainSpacingUnit = DimensionUnit.CENTIMETER,
            distDiameter = 12.0, distDiameterUnit = DimensionUnit.MILLIMETER,
            distSpacing = 15.0, distSpacingUnit = DimensionUnit.CENTIMETER
        )

        // Vol = 1.2 * 1.2 * 0.3 = 0.432 m3
        assertEquals(0.432, result.concreteVolumeM3, 0.0001)
        
        // Count = CEIL(1.2 / 0.15) + 1 = 8 + 1 = 9
        assertEquals(9, result.mainBarCount)
        assertEquals(9, result.distributionBarCount)
    }
}
