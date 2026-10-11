package com.example.probuild.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculator_favorites")
data class CalculatorFavoriteEntity(
    @PrimaryKey
    val calculatorType: String,
    val isFavorite: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
