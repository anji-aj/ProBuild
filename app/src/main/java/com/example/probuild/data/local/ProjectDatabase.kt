package com.example.probuild.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class, 
        CalculationEntity::class, 
        CalculationInputEntity::class,
        CalculatorFavoriteEntity::class,
        DiaryEntryEntity::class,
        DiaryPhotoEntity::class
    ], 
    version = 5, 
    exportSchema = false
)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun calculationDao(): CalculationDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun diaryPhotoDao(): DiaryPhotoDao
}
