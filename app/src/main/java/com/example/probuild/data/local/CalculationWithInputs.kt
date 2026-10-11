package com.example.probuild.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class CalculationWithInputs(
    @Embedded val calculation: CalculationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "calculationId"
    )
    val inputs: List<CalculationInputEntity>
)
