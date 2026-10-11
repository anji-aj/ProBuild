package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class ConcreteVolumeCalculatorTest {

    private val calculator = ConcreteVolumeCalculator()
    private val tolerance = 0.0001

    @Test
    fun calculateVolumeInMeters() {
        val result = calculator.calculate(
            5.0, DimensionUnit.METER,
            2.0, DimensionUnit.METER,
            0.2, DimensionUnit.METER
        )
        assertEquals(2.0, result.cubicMeters, tolerance)
        assertEquals(70.6293, result.cubicFeet, 0.001) // 2.0 * 35.31466...
    }

    @Test
    fun calculateVolumeWithEquivalentUnitsCm() {
        val result = calculator.calculate(
            500.0, DimensionUnit.CENTIMETER,
            200.0, DimensionUnit.CENTIMETER,
            20.0, DimensionUnit.CENTIMETER
        )
        assertEquals(2.0, result.cubicMeters, tolerance)
    }

    @Test
    fun calculateVolumeWithEquivalentUnitsFeet() {
        // 5m ~ 16.4042 ft, 2m ~ 6.5617 ft, 0.2m ~ 0.65617 ft
        // 5 / 0.3048 = 16.404199...
        val result = calculator.calculate(
            5.0 / 0.3048, DimensionUnit.FEET,
            2.0 / 0.3048, DimensionUnit.FEET,
            0.2 / 0.3048, DimensionUnit.FEET
        )
        assertEquals(2.0, result.cubicMeters, tolerance)
    }

    @Test
    fun calculateVolumeWithMixedUnits() {
        // 5m, 200cm, 0.2m
        val result = calculator.calculate(
            5.0, DimensionUnit.METER,
            200.0, DimensionUnit.CENTIMETER,
            0.2, DimensionUnit.METER
        )
        assertEquals(2.0, result.cubicMeters, tolerance)
    }

    @Test
    fun calculateVolumeWithZero() {
        val result = calculator.calculate(
            5.0, DimensionUnit.METER,
            0.0, DimensionUnit.METER,
            0.2, DimensionUnit.METER
        )
        assertEquals(0.0, result.cubicMeters, tolerance)
    }

    @Test
    fun calculateVolumeWithNegativeValues() {
        val result = calculator.calculate(
            5.0, DimensionUnit.METER,
            -2.0, DimensionUnit.METER,
            0.2, DimensionUnit.METER
        )
        assertEquals(-2.0, result.cubicMeters, tolerance)
    }
}
