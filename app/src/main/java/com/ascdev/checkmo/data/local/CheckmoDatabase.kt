package com.ascdev.checkmo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TimeEntryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CheckmoDatabase : RoomDatabase() {
    abstract fun timeEntryDao(): TimeEntryDao
}
