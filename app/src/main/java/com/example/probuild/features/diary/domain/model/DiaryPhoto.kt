package com.example.probuild.features.diary.domain.model

data class DiaryPhoto(
    val id: String,
    val diaryEntryId: String,
    val projectId: String,
    val filePath: String,
    val caption: String,
    val createdAt: Long
)
