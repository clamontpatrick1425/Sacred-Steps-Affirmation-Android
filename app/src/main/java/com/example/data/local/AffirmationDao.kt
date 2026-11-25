package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AffirmationDao {
    @Query("SELECT * FROM affirmations ORDER BY dayNumber ASC")
    fun observeAllAffirmations(): Flow<List<AffirmationEntity>>

    @Query("SELECT * FROM affirmations WHERE dayNumber = :dayNumber LIMIT 1")
    fun observeAffirmationByDay(dayNumber: Int): Flow<AffirmationEntity?>

    @Query("SELECT COUNT(*) FROM affirmations")
    suspend fun getAffirmationCount(): Int

    @Query("SELECT COUNT(*) FROM affirmations WHERE isCompleted = 1")
    fun observeCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAffirmations(items: List<AffirmationEntity>)

    @Query("UPDATE affirmations SET isCompleted = :completed WHERE dayNumber = :dayNumber")
    suspend fun setDayCompleted(dayNumber: Int, completed: Boolean)
}
