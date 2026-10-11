package com.example.probuild.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val projectName: String,
    val clientName: String,
    val location: String,
    val description: String,
    val projectType: String,
    val priority: String,
    val projectStage: String,
    val budget: String,
    val startDate: String,
    val expectedEndDate: String,
    val status: String,
    val progress: Float,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean
)
