package com.example.probuild.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.probuild.features.diary.domain.model.DiaryEntry

@Entity(
    tableName = "diary_entries",
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
data class DiaryEntryEntity(
    @PrimaryKey
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
) {
    fun toDomain(): DiaryEntry {
        return DiaryEntry(
            id = id,
            projectId = projectId,
            date = date,
            workCompleted = workCompleted,
            labourCount = labourCount,
            materialsUsed = materialsUsed,
            equipmentUsed = equipmentUsed,
            weather = weather,
            issues = issues,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(entry: DiaryEntry): DiaryEntryEntity {
            return DiaryEntryEntity(
                id = entry.id,
                projectId = entry.projectId,
                date = entry.date,
                workCompleted = entry.workCompleted,
                labourCount = entry.labourCount,
                materialsUsed = entry.materialsUsed,
                equipmentUsed = entry.equipmentUsed,
                weather = entry.weather,
                issues = entry.issues,
                notes = entry.notes,
                createdAt = entry.createdAt,
                updatedAt = entry.updatedAt
            )
        }
    }
}
