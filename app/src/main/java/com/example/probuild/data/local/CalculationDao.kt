package com.example.probuild.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Transaction
    @Query("SELECT * FROM calculations WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCalculationsByProject(projectId: String): Flow<List<CalculationWithInputs>>

    @Transaction
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCalculations(limit: Int): Flow<List<CalculationWithInputs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: CalculationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInputs(inputs: List<CalculationInputEntity>)

    @Transaction
    suspend fun insertCalculationWithInputs(calculation: CalculationEntity, inputs: List<CalculationInputEntity>) {
        insertCalculation(calculation)
        insertInputs(inputs)
    }

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteCalculation(id: String)

    @Query("UPDATE calculations SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("DELETE FROM calculations WHERE projectId = :projectId")
    suspend fun clearHistoryByProject(projectId: String)

    @Query("SELECT * FROM calculator_favorites")
    fun getFavoriteCalculators(): Flow<List<CalculatorFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: CalculatorFavoriteEntity)

    @Transaction
    @Query("SELECT * FROM calculations WHERE id = :id")
    suspend fun getCalculationById(id: String): CalculationWithInputs?
}
