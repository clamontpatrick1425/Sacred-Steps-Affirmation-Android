package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyJournalResponseDao {

    @Query("SELECT * FROM monthly_journal_responses ORDER BY monthNumber ASC, entryKey ASC")
    fun observeAllResponses(): Flow<List<MonthlyJournalResponseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResponse(response: MonthlyJournalResponseEntity)
}
