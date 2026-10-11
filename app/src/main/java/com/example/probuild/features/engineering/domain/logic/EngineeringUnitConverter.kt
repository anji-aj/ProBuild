package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.util.EngineeringConstants.M2_TO_FT2
import com.example.probuild.features.engineering.util.EngineeringConstants.M3_TO_FT3

class EngineeringUnitConverter {

    enum class Category(val displayName: String) {
        LENGTH("Length"),
        AREA("Area"),
        VOLUME("Volume"),
        MASS("Mass"),
        DENSITY("Density"),
        TEMPERATURE("Temperature")
    }

    data class Unit(val symbol: String, val factorToBase: Double = 1.0)

    val unitsByCategory = mapOf(
        Category.LENGTH to listOf(
            Unit("m", 1.0),
            Unit("cm", 0.01),
            Unit("mm", 0.001),
            Unit("ft", 0.3048),
            Unit("in", 0.0254)
        ),
        Category.AREA to listOf(
            Unit("m²", 1.0),
            Unit("cm²", 0.0001),
            Unit("mm²", 0.000001),
            Unit("ft²", 1.0 / M2_TO_FT2),
            Unit("in²", 0.0254 * 0.0254)
        ),
        Category.VOLUME to listOf(
            Unit("m³", 1.0),
            Unit("cm³", 1.0e-6),
            Unit("L", 0.001),
            Unit("ft³", 1.0 / M3_TO_FT3),
            Unit("in³", 1.6387064e-5)
        ),
        Category.MASS to listOf(
            Unit("kg", 1.0),
            Unit("g", 0.001),
            Unit("tonne", 1000.0),
            Unit("lb", 0.45359237)
        ),
        Category.DENSITY to listOf(
            Unit("kg/m³", 1.0),
            Unit("g/cm³", 1000.0),
            Unit("lb/ft³", 16.018463)
        ),
        Category.TEMPERATURE to listOf(
            Unit("°C"),
            Unit("°F"),
            Unit("K")
        )
    )

    fun convert(value: Double, fromSymbol: String, toSymbol: String, category: Category): Double {
        if (fromSymbol == toSymbol) return value

        return when (category) {
            Category.TEMPERATURE -> convertTemperature(value, fromSymbol, toSymbol)
            else -> {
                val categoryUnits = unitsByCategory[category] ?: return value
                val fromUnit = categoryUnits.find { it.symbol == fromSymbol } ?: return value
                val toUnit = categoryUnits.find { it.symbol == toSymbol } ?: return value
                
                // Convert to base, then to target
                val baseValue = value * fromUnit.factorToBase
                baseValue / toUnit.factorToBase
            }
        }
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        // First convert from 'from' to Celsius
        val celsius = when (from) {
            "°C" -> value
            "°F" -> (value - 32.0) * 5.0 / 9.0
            "K" -> value - 273.15
            else -> value
        }

        // Then convert from Celsius to 'to'
        return when (to) {
            "°C" -> celsius
            "°F" -> (celsius * 9.0 / 5.0) + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }
}
