package com.example.probuild.features.engineering.domain.model

enum class DimensionUnit(val symbol: String, val toMeterFactor: Double) {
    METER("m", 1.0),
    CENTIMETER("cm", 0.01),
    MILLIMETER("mm", 0.001),
    FEET("ft", 0.3048),
    INCH("in", 0.0254);

    companion object {
        fun fromSymbol(symbol: String): DimensionUnit {
            return entries.find { it.symbol == symbol } ?: METER
        }

        val commonLengthUnits = listOf(METER, CENTIMETER, FEET, INCH)
        val diameterUnits = listOf(MILLIMETER, CENTIMETER, INCH)
    }
}
