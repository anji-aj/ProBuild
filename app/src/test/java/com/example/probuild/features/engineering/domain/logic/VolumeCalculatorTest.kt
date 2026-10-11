package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class VolumeCalculatorTest {

    private val calculator = VolumeCalculator()

    @Test
    fun `calculateRectangularPrism - 5m x 2m x 0_2m should be 2m3`() {
        val result = calculator.calculateRectangularPrism(
            length = 5.0, lengthUnit = DimensionUnit.METER,
            width = 2.0, widthUnit = DimensionUnit.METER,
            height = 0.2, heightUnit = DimensionUnit.METER
        )
        assertEquals(2.0, result.volumeM3, 0.0001)
    }

    @Test
    fun `calculateCube - 10 feet`() {
        // 10 feet = 3.048 meters
        // Volume = 3.048^3 = 28.3168 m3
        val result = calculator.calculateCube(
            side = 10.0, sideUnit = DimensionUnit.FEET
        )
        val expected = 3.048 * 3.048 * 3.048
        assertEquals(expected, result.volumeM3, 0.0001)
    }

    @Test
    fun `calculateCylinder - r=1m h=2m`() {
        val result = calculator.calculateCylinder(
            radius = 1.0, radiusUnit = DimensionUnit.METER,
            height = 2.0, heightUnit = DimensionUnit.METER
        )
        // PI * 1^2 * 2 = 2 * PI
        assertEquals(2 * PI, result.volumeM3, 0.000001)
        assertEquals(6.283185, result.volumeM3, 0.000001)
    }

    @Test
    fun `calculateSphere - r=1m`() {
        val result = calculator.calculateSphere(
            radius = 1.0, radiusUnit = DimensionUnit.METER
        )
        // (4/3) * PI * 1^3 = 4/3 * PI
        assertEquals((4.0/3.0) * PI, result.volumeM3, 0.000001)
        assertEquals(4.188790, result.volumeM3, 0.000001)
    }

    @Test
    fun `calculate - mixed units`() {
        // Prism: 100cm x 1m x 12in
        // 1m x 1m x 0.3048m = 0.3048 m3
        val result = calculator.calculateRectangularPrism(
            length = 100.0, lengthUnit = DimensionUnit.CENTIMETER,
            width = 1.0, widthUnit = DimensionUnit.METER,
            height = 12.0, heightUnit = DimensionUnit.INCH
        )
        assertEquals(0.3048, result.volumeM3, 0.0001)
    }
}
