package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun observeAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE dayNumber = :dayNumber ORDER BY timestamp DESC")
    fun observeJournalEntriesForDay(dayNumber: Int): Flow<List<JournalEntryEntity>>

    @Query("SELECT DISTINCT dayNumber FROM journal_entries")
    fun observeJournaledDays(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity)

    @Update
    suspend fun updateJournalEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntry(id: Int)
}
