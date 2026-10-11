package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class BrickCalculatorTest {

    private val calculator = BrickCalculator()

    @Test
    fun `calculate - basic m and mm scenario`() {
        // Wall: 5m x 3m x 0.20m = 3m3
        // Brick: 190mm x 90mm x 90mm -> 19cm x 9cm x 9cm
        // Joint: 10mm -> 1cm
        // Wastage: 5%
        
        val result = calculator.calculate(
            wallLength = 5.0, wallLengthUnit = DimensionUnit.METER,
            wallHeight = 3.0, wallHeightUnit = DimensionUnit.METER,
            wallThickness = 0.20, wallThicknessUnit = DimensionUnit.METER,
            brickLength = 19.0, brickLengthUnit = DimensionUnit.CENTIMETER,
            brickWidth = 9.0, brickWidthUnit = DimensionUnit.CENTIMETER,
            brickHeight = 9.0, brickHeightUnit = DimensionUnit.CENTIMETER,
            mortarJoint = 1.0, mortarJointUnit = DimensionUnit.CENTIMETER,
            wastagePercentage = 5.0
        )

        // Wall Vol = 3.0
        // Brick+Mortar = (0.19+0.01) * (0.09+0.01) * (0.09+0.01) = 0.2 * 0.1 * 0.1 = 0.002
        // Base count = 3.0 / 0.002 = 1500
        // Final count = 1500 * 1.05 = 1575
        
        assertEquals(1575, result.totalBricks)
        assertEquals(3.0, result.wallVolume, 0.0001)
        assertEquals(0.002, result.brickWithMortarVolume, 0.000001)
    }

    @Test
    fun `calculate - check ceiling rounding`() {
        // Wall: 1m x 1m x 0.1m = 0.1m3
        // Brick: 20cm x 10cm x 10cm
        // Joint: 0
        // Wastage: 0
        
        // Brick Vol = 0.2 * 0.1 * 0.1 = 0.002
        // Count = 0.1 / 0.002 = 50
        
        val result = calculator.calculate(
            wallLength = 1.0, wallLengthUnit = DimensionUnit.METER,
            wallHeight = 1.0, wallHeightUnit = DimensionUnit.METER,
            wallThickness = 0.1, wallThicknessUnit = DimensionUnit.METER,
            brickLength = 20.0, brickLengthUnit = DimensionUnit.CENTIMETER,
            brickWidth = 10.0, brickWidthUnit = DimensionUnit.CENTIMETER,
            brickHeight = 10.0, brickHeightUnit = DimensionUnit.CENTIMETER,
            mortarJoint = 0.0, mortarJointUnit = DimensionUnit.CENTIMETER,
            wastagePercentage = 0.0
        )
        
        assertEquals(50, result.totalBricks)
    }
}
