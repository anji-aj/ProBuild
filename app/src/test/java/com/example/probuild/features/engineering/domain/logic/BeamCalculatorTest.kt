package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class BeamCalculatorTest {

    private val calculator = BeamCalculator()

    @Test
    fun `calculate - basic metric verification`() {
        // Beam: 5m x 0.3m x 0.5m = 0.75m3
        // Main Bars: 4 bars, 16mm diameter
        // Stirrups: 8mm diameter, 200mm spacing
        
        val result = calculator.calculate(
            length = 5.0, lengthUnit = DimensionUnit.METER,
            width = 30.0, widthUnit = DimensionUnit.CENTIMETER,
            depth = 50.0, depthUnit = DimensionUnit.CENTIMETER,
            mainBarCount = 4,
            mainBarDiameter = 16.0, mainBarDiameterUnit = DimensionUnit.MILLIMETER,
            stirrupDiameter = 8.0, stirrupDiameterUnit = DimensionUnit.MILLIMETER,
            stirrupSpacing = 200.0, stirrupSpacingUnit = DimensionUnit.MILLIMETER
        )

        // 1. Concrete Volume = 5 * 0.3 * 0.5 = 0.75 m3
        assertEquals(0.75, result.concreteVolumeM3, 0.0001)

        // 2. Main Steel Weight
        // d = 0.016m, Area = PI * 0.016^2 / 4 = 0.00020106 m2
        // Weight = 4 * 5 * 0.00020106 * 7850 = 31.566 kg
        val expectedMainArea = PI * 0.016 * 0.016 / 4.0
        val expectedMainWeight = 4 * 5 * expectedMainArea * 7850.0
        assertEquals(expectedMainWeight, result.mainSteelWeightKg, 0.01)

        // 3. Stirrup Count = CEIL(5 / 0.2) + 1 = 25 + 1 = 26
        assertEquals(26, result.stirrupCount)

        // 4. Stirrup Length = 2 * (0.3 + 0.5) = 1.6 m
        // 5. Stirrup Weight
        // d = 0.008m, Area = PI * 0.008^2 / 4 = 0.000050265 m2
        // Weight = 26 * 1.6 * 0.000050265 * 7850 = 16.417 kg
        val expectedStirrupArea = PI * 0.008 * 0.008 / 4.0
        val expectedStirrupWeight = 26 * 1.6 * expectedStirrupArea * 7850.0
        assertEquals(expectedStirrupWeight, result.stirrupWeightKg, 0.01)
        
        assertEquals(expectedMainWeight + expectedStirrupWeight, result.totalSteelWeightKg, 0.01)
    }

    @Test
    fun `calculate - mixed units verification`() {
        // 10ft beam, 12in x 18in
        // 10ft = 3.048m
        // 12in = 0.3048m
        // 18in = 0.4572m
        
        val result = calculator.calculate(
            length = 10.0, lengthUnit = DimensionUnit.FEET,
            width = 12.0, widthUnit = DimensionUnit.INCH,
            depth = 18.0, depthUnit = DimensionUnit.INCH,
            mainBarCount = 2,
            mainBarDiameter = 0.5, mainBarDiameterUnit = DimensionUnit.INCH,
            stirrupDiameter = 10.0, stirrupDiameterUnit = DimensionUnit.MILLIMETER,
            stirrupSpacing = 6.0, stirrupSpacingUnit = DimensionUnit.INCH
        )
        
        // Vol = 3.048 * 0.3048 * 0.4572 = 0.42475 m3
        assertEquals(0.42475, result.concreteVolumeM3, 0.001)
        
        // Stirrup Count = CEIL(3.048 / (6 * 0.0254)) + 1 = CEIL(3.048 / 0.1524) + 1 = 20 + 1 = 21
        assertEquals(21, result.stirrupCount)
    }
}
