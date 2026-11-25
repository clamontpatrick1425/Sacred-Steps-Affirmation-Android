package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkbookNoteDao {
    @Query("SELECT * FROM workbook_notes")
    fun observeAllWorkbookNotes(): Flow<List<WorkbookNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkbookNote(note: WorkbookNoteEntity)
}
