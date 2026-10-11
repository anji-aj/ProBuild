package com.example.probuild.features.diary.domain

import android.net.Uri
import com.example.probuild.features.diary.domain.model.DiaryPhoto
import kotlinx.coroutines.flow.Flow

interface DiaryPhotoRepository {
    fun observePhotosByDiaryEntry(entryId: String): Flow<List<DiaryPhoto>>
    suspend fun getPhotoById(id: String): DiaryPhoto?
    
    /**
     * Copies photo from [sourceUri] to internal storage and saves metadata.
     */
    suspend fun addPhoto(diaryEntryId: String, projectId: String, sourceUri: Uri): Result<DiaryPhoto>
    
    suspend fun updateCaption(photoId: String, newCaption: String)
    suspend fun deletePhoto(photoId: String)
    suspend fun deletePhotosByDiaryEntry(entryId: String)
}
