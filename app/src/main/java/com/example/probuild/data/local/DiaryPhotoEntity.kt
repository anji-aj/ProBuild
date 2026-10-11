package com.example.probuild.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_photos",
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["diaryEntryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["diaryEntryId"]),
        Index(value = ["projectId"])
    ]
)
data class DiaryPhotoEntity(
    @PrimaryKey
    val id: String,
    val diaryEntryId: String,
    val projectId: String,
    val filePath: String,
    val caption: String,
    val createdAt: Long
)
