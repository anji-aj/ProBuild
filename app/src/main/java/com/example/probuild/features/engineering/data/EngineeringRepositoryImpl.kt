package com.example.probuild.features.engineering.data

import com.example.probuild.data.local.CalculationDao
import com.example.probuild.data.local.CalculatorFavoriteEntity
import com.example.probuild.features.engineering.data.mapper.toDomain
import com.example.probuild.features.engineering.data.mapper.toEntity
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class EngineeringRepositoryImpl(
    private val dao: CalculationDao
) : EngineeringRepository {
    
    override fun getCalculationsByProject(projectId: String): Flow<List<Calculation>> {
        return dao.getCalculationsByProject(projectId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertCalculation(calculation: Calculation) {
        val (entity, inputs) = calculation.toEntity()
        dao.insertCalculationWithInputs(entity, inputs)
    }

    override suspend fun deleteCalculation(id: String) {
        dao.deleteCalculation(id)
    }

    override suspend fun toggleFavorite(id: String) {
        dao.toggleFavorite(id)
    }

    override fun getRecentCalculations(limit: Int): Flow<List<Calculation>> {
        return dao.getRecentCalculations(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteCalculators(): Flow<Set<CalculationType>> {
        return dao.getFavoriteCalculators().map { entities ->
            entities.filter { it.isFavorite }
                .mapNotNull { 
                    try {
                        CalculationType.valueOf(it.calculatorType)
                    } catch (e: Exception) {
                        null
                    }
                }.toSet()
        }
    }

    override suspend fun toggleCalculatorFavorite(type: CalculationType) {
        val currentFavorites = dao.getFavoriteCalculators().first()
        val existing = currentFavorites.find { it.calculatorType == type.name }
        val newState = !(existing?.isFavorite ?: false)
        
        dao.insertFavorite(
            CalculatorFavoriteEntity(
                calculatorType = type.name,
                isFavorite = newState
            )
        )
    }

    override suspend fun getCalculationById(id: String): Calculation? {
        return dao.getCalculationById(id)?.toDomain()
    }
}
