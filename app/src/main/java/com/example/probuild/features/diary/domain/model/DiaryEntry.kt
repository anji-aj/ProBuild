package com.example.probuild.features.diary.domain.model

data class DiaryEntry(
    val id: String,
    val projectId: String,
    val date: Long,
    val workCompleted: String,
    val labourCount: Int,
    val materialsUsed: String,
    val equipmentUsed: String,
    val weather: String,
    val issues: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)
