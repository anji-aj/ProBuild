package com.example.probuild.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries WHERE projectId = :projectId ORDER BY date DESC, createdAt DESC")
    fun observeEntriesByProject(projectId: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getEntryById(id: String): DiaryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity)

    @Update
    suspend fun updateEntry(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("""
        SELECT * FROM diary_entries 
        WHERE projectId = :projectId 
        AND (workCompleted LIKE '%' || :query || '%' 
             OR materialsUsed LIKE '%' || :query || '%' 
             OR equipmentUsed LIKE '%' || :query || '%' 
             OR issues LIKE '%' || :query || '%' 
             OR notes LIKE '%' || :query || '%')
        ORDER BY date DESC, createdAt DESC
    """)
    fun searchEntries(projectId: String, query: String): Flow<List<DiaryEntryEntity>>
}
