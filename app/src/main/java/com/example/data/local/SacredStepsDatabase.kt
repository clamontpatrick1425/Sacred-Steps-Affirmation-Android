package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AffirmationEntity::class,
        JournalEntryEntity::class,
        MonthlyJournalResponseEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SacredStepsDatabase : RoomDatabase() {
    abstract fun affirmationDao(): AffirmationDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun monthlyJournalResponseDao(): MonthlyJournalResponseDao
}
