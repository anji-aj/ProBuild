package com.example.probuild.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calculations",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class CalculationEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val type: String,
    val displayName: String,
    val formulaVersion: String,
    val resultVal: Double,
    val resultUnit: String,
    val timestamp: Long,
    val isFavorite: Boolean
)

@Entity(
    tableName = "calculation_inputs",
    foreignKeys = [
        ForeignKey(
            entity = CalculationEntity::class,
            parentColumns = ["id"],
            childColumns = ["calculationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["calculationId"])]
)
data class CalculationInputEntity(
    @PrimaryKey
    val id: String,
    val calculationId: String,
    val inputKey: String,
    val valAmount: Double,
    val unit: String
)
