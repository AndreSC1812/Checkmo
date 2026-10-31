package com.ascdev.checkmo.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TimeEntryEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2: location where each entry was recorded.
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class CheckmoDatabase : RoomDatabase() {
    abstract fun timeEntryDao(): TimeEntryDao
}
