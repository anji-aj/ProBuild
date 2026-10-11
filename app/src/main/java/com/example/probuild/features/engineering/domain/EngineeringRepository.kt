package com.example.probuild.features.engineering.domain

import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationType
import kotlinx.coroutines.flow.Flow

interface EngineeringRepository {
    fun getCalculationsByProject(projectId: String): Flow<List<Calculation>>
    suspend fun insertCalculation(calculation: Calculation)
    suspend fun deleteCalculation(id: String)
    suspend fun toggleFavorite(id: String)
    fun getRecentCalculations(limit: Int): Flow<List<Calculation>>
    
    fun getFavoriteCalculators(): Flow<Set<CalculationType>>
    suspend fun toggleCalculatorFavorite(type: CalculationType)

    suspend fun getCalculationById(id: String): Calculation?
}
