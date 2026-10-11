package com.example.probuild.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryPhotoDao {
    @Query("SELECT * FROM diary_photos WHERE diaryEntryId = :entryId ORDER BY createdAt ASC")
    fun observePhotosByDiaryEntry(entryId: String): Flow<List<DiaryPhotoEntity>>

    @Query("SELECT * FROM diary_photos WHERE diaryEntryId = :entryId ORDER BY createdAt ASC")
    suspend fun getPhotosByDiaryEntrySync(entryId: String): List<DiaryPhotoEntity>

    @Query("SELECT * FROM diary_photos WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun observePhotosByProject(projectId: String): Flow<List<DiaryPhotoEntity>>

    @Query("SELECT * FROM diary_photos WHERE id = :id")
    suspend fun getPhotoById(id: String): DiaryPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: DiaryPhotoEntity)

    @Update
    suspend fun updatePhoto(photo: DiaryPhotoEntity)

    @Query("DELETE FROM diary_photos WHERE id = :id")
    suspend fun deletePhoto(id: String)

    @Query("DELETE FROM diary_photos WHERE diaryEntryId = :entryId")
    suspend fun deletePhotosByDiaryEntry(entryId: String)
}
