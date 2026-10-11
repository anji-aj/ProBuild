package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class ColumnCalculatorTest {

    private val calculator = ColumnCalculator()

    @Test
    fun `calculate - basic metric verification`() {
        // Column: 3m height, 0.4m width, 0.4m depth = 0.48m3
        // Main Bars: 4 bars, 16mm diameter
        // Ties: 8mm diameter, 200mm spacing
        
        val result = calculator.calculate(
            height = 3.0, heightUnit = DimensionUnit.METER,
            width = 40.0, widthUnit = DimensionUnit.CENTIMETER,
            depth = 40.0, depthUnit = DimensionUnit.CENTIMETER,
            mainBarCount = 4,
            mainBarDiameter = 16.0, mainBarDiameterUnit = DimensionUnit.MILLIMETER,
            tieDiameter = 8.0, tieDiameterUnit = DimensionUnit.MILLIMETER,
            tieSpacing = 200.0, tieSpacingUnit = DimensionUnit.MILLIMETER
        )

        // 1. Concrete Volume = 3 * 0.4 * 0.4 = 0.48 m3
        assertEquals(0.48, result.concreteVolumeM3, 0.0001)

        // 2. Main Steel Weight
        // d = 0.016m, Area = PI * 0.016^2 / 4 = 0.00020106 m2
        // Weight = 4 * 3 * 0.00020106 * 7850 = 18.939 kg
        val expectedMainArea = PI * 0.016 * 0.016 / 4.0
        val expectedMainWeight = 4 * 3 * expectedMainArea * 7850.0
        assertEquals(expectedMainWeight, result.mainSteelWeightKg, 0.01)

        // 3. Tie Count = CEIL(3 / 0.2) + 1 = 15 + 1 = 16
        assertEquals(16, result.tieCount)

        // 4. Tie Length = 2 * (0.4 + 0.4) = 1.6 m
        // 5. Tie Weight
        // d = 0.008m, Area = PI * 0.008^2 / 4 = 0.000050265 m2
        // Weight = 16 * 1.6 * 0.000050265 * 7850 = 10.103 kg
        val expectedTieArea = PI * 0.008 * 0.008 / 4.0
        val expectedTieWeight = 16 * 1.6 * expectedTieArea * 7850.0
        assertEquals(expectedTieWeight, result.tieWeightKg, 0.01)
        
        assertEquals(expectedMainWeight + expectedTieWeight, result.totalSteelWeightKg, 0.01)
    }

    @Test
    fun `calculate - mixed units verification`() {
        // 10ft column, 12in x 12in
        // 10ft = 3.048m
        // 12in = 0.3048m
        
        val result = calculator.calculate(
            height = 10.0, heightUnit = DimensionUnit.FEET,
            width = 12.0, widthUnit = DimensionUnit.INCH,
            depth = 12.0, depthUnit = DimensionUnit.INCH,
            mainBarCount = 4,
            mainBarDiameter = 0.5, mainBarDiameterUnit = DimensionUnit.INCH,
            tieDiameter = 10.0, tieDiameterUnit = DimensionUnit.MILLIMETER,
            tieSpacing = 6.0, tieSpacingUnit = DimensionUnit.INCH
        )
        
        // Vol = 3.048 * 0.3048 * 0.3048 = 0.28317 m3
        assertEquals(0.28317, result.concreteVolumeM3, 0.001)
        
        // Tie Count = CEIL(3.048 / (6 * 0.0254)) + 1 = CEIL(3.048 / 0.1524) + 1 = 20 + 1 = 21
        assertEquals(21, result.tieCount)
    }
}
