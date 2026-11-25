package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "affirmations")
data class AffirmationEntity(
    @PrimaryKey val dayNumber: Int, // 1..365
    val weekNumber: Int, // 1..52
    val monthNumber: Int, // 1..12
    val monthName: String,
    val title: String,
    val scripture: String,
    val truthReframe: String,
    val affirmationText: String,
    val practiceAction: String,
    val reflectionPrompt: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayNumber: Int,
    val entryText: String,
    val heartMood: String = "",
    val timestamp: Long
)

@Entity(tableName = "monthly_journal_responses", primaryKeys = ["monthNumber", "entryKey"])
data class MonthlyJournalResponseEntity(
    val monthNumber: Int,
    val entryKey: String,
    val content: String,
    val updatedAt: Long
)
