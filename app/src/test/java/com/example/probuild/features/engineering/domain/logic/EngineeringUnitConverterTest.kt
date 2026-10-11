package com.example.probuild.features.engineering.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class EngineeringUnitConverterTest {

    private val converter = EngineeringUnitConverter()

    @Test
    fun `length conversion - 5m to ft`() {
        val result = converter.convert(5.0, "m", "ft", EngineeringUnitConverter.Category.LENGTH)
        // 5 * 3.28084 = 16.4042
        assertEquals(16.4042, result, 0.001)
    }

    @Test
    fun `area conversion - 1m2 to ft2`() {
        val result = converter.convert(1.0, "m²", "ft²", EngineeringUnitConverter.Category.AREA)
        assertEquals(10.7639, result, 0.0001)
    }

    @Test
    fun `volume conversion - 1m3 to ft3`() {
        val result = converter.convert(1.0, "m³", "ft³", EngineeringUnitConverter.Category.VOLUME)
        assertEquals(35.3147, result, 0.0001)
    }

    @Test
    fun `mass conversion - 1kg to lb`() {
        val result = converter.convert(1.0, "kg", "lb", EngineeringUnitConverter.Category.MASS)
        assertEquals(2.20462, result, 0.0001)
    }

    @Test
    fun `density conversion - 1000kg m3 to g cm3`() {
        val result = converter.convert(1000.0, "kg/m³", "g/cm³", EngineeringUnitConverter.Category.DENSITY)
        assertEquals(1.0, result, 0.0001)
    }

    @Test
    fun `temperature conversion - celsius to fahrenheit`() {
        val result = converter.convert(0.0, "°C", "°F", EngineeringUnitConverter.Category.TEMPERATURE)
        assertEquals(32.0, result, 0.0001)
        
        val result2 = converter.convert(100.0, "°C", "°F", EngineeringUnitConverter.Category.TEMPERATURE)
        assertEquals(212.0, result2, 0.0001)
    }

    @Test
    fun `temperature conversion - celsius to kelvin`() {
        val result = converter.convert(0.0, "°C", "K", EngineeringUnitConverter.Category.TEMPERATURE)
        assertEquals(273.15, result, 0.0001)
    }

    @Test
    fun `temperature conversion - fahrenheit to celsius`() {
        val result = converter.convert(32.0, "°F", "°C", EngineeringUnitConverter.Category.TEMPERATURE)
        assertEquals(0.0, result, 0.0001)
    }

    @Test
    fun `precision - 100cm to m`() {
        val result = converter.convert(100.0, "cm", "m", EngineeringUnitConverter.Category.LENGTH)
        assertEquals(1.0, result, 0.0)
    }
}
