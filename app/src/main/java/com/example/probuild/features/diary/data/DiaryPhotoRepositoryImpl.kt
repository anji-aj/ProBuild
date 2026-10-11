package com.example.probuild.features.diary.data

import android.content.Context
import android.net.Uri
import com.example.probuild.data.local.DiaryPhotoDao
import com.example.probuild.data.local.DiaryPhotoEntity
import com.example.probuild.features.diary.domain.DiaryPhotoRepository
import com.example.probuild.features.diary.domain.model.DiaryPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DiaryPhotoRepositoryImpl(
    private val context: Context,
    private val diaryPhotoDao: DiaryPhotoDao
) : DiaryPhotoRepository {

    private val photosDir: File by lazy {
        File(context.filesDir, "diary_photos").apply {
            if (!exists()) mkdirs()
        }
    }

    override fun observePhotosByDiaryEntry(entryId: String): Flow<List<DiaryPhoto>> {
        return diaryPhotoDao.observePhotosByDiaryEntry(entryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPhotoById(id: String): DiaryPhoto? {
        return diaryPhotoDao.getPhotoById(id)?.toDomain()
    }

    override suspend fun addPhoto(diaryEntryId: String, projectId: String, sourceUri: Uri): Result<DiaryPhoto> = withContext(Dispatchers.IO) {
        try {
            val photoId = UUID.randomUUID().toString()
            val fileName = "photo_${photoId}.jpg"
            val destFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Could not open source URI"))

            if (!destFile.exists() || destFile.length() == 0L) {
                return@withContext Result.failure(Exception("Failed to copy image file"))
            }

            val entity = DiaryPhotoEntity(
                id = photoId,
                diaryEntryId = diaryEntryId,
                projectId = projectId,
                filePath = destFile.absolutePath,
                caption = "",
                createdAt = System.currentTimeMillis()
            )
            diaryPhotoDao.insertPhoto(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCaption(photoId: String, newCaption: String): Unit = withContext(Dispatchers.IO) {
        try {
            diaryPhotoDao.getPhotoById(photoId)?.let {
                diaryPhotoDao.updatePhoto(it.copy(caption = newCaption))
            }
        } catch (e: Exception) {
            // Gracefully handle Room operation failure
        }
    }

    override suspend fun deletePhoto(photoId: String): Unit = withContext(Dispatchers.IO) {
        try {
            diaryPhotoDao.getPhotoById(photoId)?.let { entity ->
                try {
                    val file = File(entity.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Gracefully handle missing/already deleted files
                }
                diaryPhotoDao.deletePhoto(photoId)
            }
        } catch (e: Exception) {
            // Gracefully handle Room operation failure
        }
    }

    override suspend fun deletePhotosByDiaryEntry(entryId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val photos = diaryPhotoDao.getPhotosByDiaryEntrySync(entryId)
            photos.forEach { entity ->
                try {
                    val file = File(entity.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Gracefully handle missing/already deleted files
                }
            }
            diaryPhotoDao.deletePhotosByDiaryEntry(entryId)
        } catch (e: Exception) {
            // Gracefully handle Room operation failure
        }
    }

    private fun DiaryPhotoEntity.toDomain() = DiaryPhoto(
        id = id,
        diaryEntryId = diaryEntryId,
        projectId = projectId,
        filePath = filePath,
        caption = caption,
        createdAt = createdAt
    )
}
