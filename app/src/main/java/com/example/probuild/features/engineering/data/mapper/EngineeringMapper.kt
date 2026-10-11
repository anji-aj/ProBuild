package com.example.probuild.features.engineering.data.mapper

import com.example.probuild.data.local.CalculationEntity
import com.example.probuild.data.local.CalculationInputEntity
import com.example.probuild.data.local.CalculationWithInputs
import com.example.probuild.features.engineering.domain.model.*

import com.example.probuild.features.projects.data.mapper.safeEnumValueOf

fun CalculationWithInputs.toDomain(): Calculation {
    return Calculation(
        id = calculation.id,
        projectId = calculation.projectId,
        type = safeEnumValueOf<CalculationType>(calculation.type, CalculationType.CONCRETE),
        displayName = calculation.displayName,
        formulaVersion = calculation.formulaVersion,
        resultVal = calculation.resultVal,
        resultUnit = calculation.resultUnit,
        timestamp = calculation.timestamp,
        isFavorite = calculation.isFavorite,
        inputs = inputs.map { it.toDomain() }
    )
}

fun CalculationInputEntity.toDomain(): CalculationInput {
    return CalculationInput(
        id = id,
        key = inputKey,
        value = valAmount,
        unit = unit
    )
}

fun Calculation.toEntity(): Pair<CalculationEntity, List<CalculationInputEntity>> {
    val entity = CalculationEntity(
        id = id,
        projectId = projectId,
        type = type.name,
        displayName = displayName,
        formulaVersion = formulaVersion,
        resultVal = resultVal,
        resultUnit = resultUnit,
        timestamp = timestamp,
        isFavorite = isFavorite
    )
    val inputEntities = inputs.map { 
        CalculationInputEntity(
            id = it.id,
            calculationId = id,
            inputKey = it.key,
            valAmount = it.value,
            unit = it.unit
        )
    }
    return entity to inputEntities
}
