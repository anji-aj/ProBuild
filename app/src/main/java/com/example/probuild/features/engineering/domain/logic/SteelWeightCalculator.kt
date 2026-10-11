package com.example.probuild.features.engineering.domain.logic

import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.util.EngineeringConstants.STEEL_DENSITY
import com.example.probuild.features.engineering.util.UnitConverter
import kotlin.math.PI

class SteelWeightCalculator {

    data class CalculationResult(
        val weightKg: Double,
        val volumeM3: Double,
        val totalBars: Int
    )

    fun calculate(
        diameter: Double,
        diameterUnit: DimensionUnit,
        length: Double,
        lengthUnit: DimensionUnit,
        numberOfBars: Int
    ): CalculationResult {
        // Convert to meters
        val dMeters = UnitConverter.toMeters(diameter, diameterUnit)
        val lMeters = UnitConverter.toMeters(length, lengthUnit)

        // Area = PI * d^2 / 4
        val area = PI * (dMeters * dMeters) / 4.0

        // Volume = Area * Length * Number of Bars
        val volume = area * lMeters * numberOfBars

        // Weight = Volume * Density (7850 kg/m3)
        val weight = volume * STEEL_DENSITY

        return CalculationResult(
            weightKg = weight,
            volumeM3 = volume,
            totalBars = numberOfBars
        )
    }
}
