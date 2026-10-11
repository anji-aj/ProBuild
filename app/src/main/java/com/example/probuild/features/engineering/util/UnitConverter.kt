package com.example.probuild.features.engineering.util

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.EngineeringConstants.M2_TO_FT2
import com.example.probuild.features.engineering.util.EngineeringConstants.M3_TO_FT3

object UnitConverter {

    fun toMeters(value: Double, unit: DimensionUnit): Double {
        return value * unit.toMeterFactor
    }

    fun fromMeters(meters: Double, unit: DimensionUnit): Double {
        return meters / unit.toMeterFactor
    }

    fun squareMetersToSquareFeet(m2: Double): Double {
        return m2 * M2_TO_FT2
    }

    fun cubicMetersToCubicFeet(m3: Double): Double {
        return m3 * M3_TO_FT3
    }
}
