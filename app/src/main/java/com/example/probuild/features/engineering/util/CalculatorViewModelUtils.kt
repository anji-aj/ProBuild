package com.example.probuild.features.engineering.util

import com.example.probuild.features.engineering.domain.model.DimensionUnit

object CalculatorViewModelUtils {
    fun convertValue(valueStr: String, fromUnit: DimensionUnit, toUnit: DimensionUnit): String {
        val value = valueStr.toDoubleOrNull() ?: return valueStr
        val meters = UnitConverter.toMeters(value, fromUnit)
        val result = UnitConverter.fromMeters(meters, toUnit)
        
        return if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            "%.4f".format(result).trimEnd('0').trimEnd('.')
        }
    }
}
