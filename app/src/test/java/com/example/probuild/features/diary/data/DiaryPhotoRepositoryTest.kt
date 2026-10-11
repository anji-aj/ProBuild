package com.example.probuild.features.diary.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.probuild.data.local.DiaryPhotoDao
import com.example.probuild.data.local.DiaryPhotoEntity
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class DiaryPhotoRepositoryTest {

    private val context = mockk<Context>(relaxed = true)
    private val diaryPhotoDao = mockk<DiaryPhotoDao>(relaxed = true)
    private lateinit var repository: DiaryPhotoRepositoryImpl
    private val testDir = File("build/test_photos")

    @Before
    fun setup() {
        if (testDir.exists()) testDir.deleteRecursively()
        testDir.mkdirs()
        every { context.filesDir } returns testDir
        repository = DiaryPhotoRepositoryImpl(context, diaryPhotoDao)
    }

    @Test
    fun `addPhoto - copies file and saves metadata with project and entry id`() = runTest {
        val sourceUri = mockk<Uri>()
        val mockInputStream = ByteArrayInputStream("dummy image content".toByteArray())
        val contentResolver = mockk<ContentResolver>()
        
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(sourceUri) } returns mockInputStream
        
        val result = repository.addPhoto("entry1", "proj1", sourceUri)
        
        assertTrue(result.isSuccess)
        val photo = result.getOrNull()
        assertNotNull(photo)
        assertEquals("entry1", photo!!.diaryEntryId)
        assertEquals("proj1", photo.projectId)
        assertTrue(File(photo.filePath).exists())
        
        coVerify { diaryPhotoDao.insertPhoto(match { 
            it.id == photo.id && it.diaryEntryId == "entry1" && it.projectId == "proj1" 
        }) }
    }

    @Test
    fun `observePhotosByDiaryEntry - queries by entry id and maps to domain`() = runTest {
        val entity = DiaryPhotoEntity("p1", "entry1", "proj1", "/path/p1.jpg", "Caption 1", 100L)
        every { diaryPhotoDao.observePhotosByDiaryEntry("entry1") } returns flowOf(listOf(entity))

        val photos = repository.observePhotosByDiaryEntry("entry1").first()

        assertEquals(1, photos.size)
        assertEquals("p1", photos[0].id)
        assertEquals("entry1", photos[0].diaryEntryId)
        assertEquals("proj1", photos[0].projectId)
        assertEquals("Caption 1", photos[0].caption)
    }

    @Test
    fun `updateCaption - updates photo record in database`() = runTest {
        val entity = DiaryPhotoEntity("p1", "entry1", "proj1", "/path/p1.jpg", "Old Caption", 100L)
        coEvery { diaryPhotoDao.getPhotoById("p1") } returns entity

        repository.updateCaption("p1", "New Caption")

        coVerify { diaryPhotoDao.updatePhoto(match { 
            it.id == "p1" && it.caption == "New Caption" 
        }) }
    }

    @Test
    fun `deletePhoto - performs physical file cleanup and database record deletion`() = runTest {
        val photoFile = File(testDir, "test_delete.jpg").apply { writeText("data") }
        val entity = DiaryPhotoEntity("p1", "entry1", "proj1", photoFile.absolutePath, "", 100L)
        coEvery { diaryPhotoDao.getPhotoById("p1") } returns entity

        assertTrue(photoFile.exists())

        repository.deletePhoto("p1")

        assertFalse(photoFile.exists())
        coVerify { diaryPhotoDao.deletePhoto("p1") }
    }

    @Test
    fun `deletePhotosByDiaryEntry - cleans up all physical files and removes database records`() = runTest {
        val file1 = File(testDir, "p1.jpg").apply { writeText("img1") }
        val file2 = File(testDir, "p2.jpg").apply { writeText("img2") }
        val entities = listOf(
            DiaryPhotoEntity("p1", "entry1", "proj1", file1.absolutePath, "", 100L),
            DiaryPhotoEntity("p2", "entry1", "proj1", file2.absolutePath, "", 200L)
        )
        coEvery { diaryPhotoDao.getPhotosByDiaryEntrySync("entry1") } returns entities

        repository.deletePhotosByDiaryEntry("entry1")

        assertFalse(file1.exists())
        assertFalse(file2.exists())
        coVerify { diaryPhotoDao.deletePhotosByDiaryEntry("entry1") }
    }

    @Test
    fun `missing file handling - delete operations complete gracefully without exception`() = runTest {
        val missingFilePath = File(testDir, "non_existent_file.jpg").absolutePath
        val entity = DiaryPhotoEntity("p_missing", "entry1", "proj1", missingFilePath, "", 100L)
        coEvery { diaryPhotoDao.getPhotoById("p_missing") } returns entity
        coEvery { diaryPhotoDao.getPhotosByDiaryEntrySync("entry1") } returns listOf(entity)

        // Should not throw exception
        repository.deletePhoto("p_missing")
        coVerify { diaryPhotoDao.deletePhoto("p_missing") }

        repository.deletePhotosByDiaryEntry("entry1")
        coVerify { diaryPhotoDao.deletePhotosByDiaryEntry("entry1") }
    }

    @Test
    fun `project isolation - photos store both project and entry IDs correctly`() = runTest {
        val sourceUri = mockk<Uri>()
        val mockInputStream = ByteArrayInputStream("image data".toByteArray())
        val contentResolver = mockk<ContentResolver>()
        
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(sourceUri) } returns mockInputStream

        val resultProjectA = repository.addPhoto("entryA", "projectA", sourceUri)
        assertTrue(resultProjectA.isSuccess)
        assertEquals("projectA", resultProjectA.getOrNull()?.projectId)
        assertEquals("entryA", resultProjectA.getOrNull()?.diaryEntryId)
    }
}
