package com.example.data.repository

import com.example.data.local.AffirmationDao
import com.example.data.local.JournalEntryDao
import com.example.data.local.JournalEntryEntity
import com.example.data.local.MonthlyJournalResponseDao
import com.example.data.local.MonthlyJournalResponseEntity
import com.example.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalRepository(
    private val journalEntryDao: JournalEntryDao,
    private val monthlyJournalResponseDao: MonthlyJournalResponseDao,
    private val affirmationDao: AffirmationDao
) {
    val allJournalEntries: Flow<List<JournalEntry>> = journalEntryDao.observeAllJournalEntries().map { list ->
        list.map { it.toDomain() }
    }

    fun observeEntriesForDay(dayNumber: Int): Flow<List<JournalEntry>> {
        return journalEntryDao.observeJournalEntriesForDay(dayNumber).map { list ->
            list.map { it.toDomain() }
        }
    }

    val journaledDays: Flow<Set<Int>> = journalEntryDao.observeJournaledDays().map { it.toSet() }

    val monthlyJournalResponsesMap: Flow<Map<String, String>> =
        monthlyJournalResponseDao.observeAllResponses().map { list ->
            list.associate { "${it.monthNumber}:${it.entryKey}" to it.content }
        }

    suspend fun saveJournalEntry(dayNumber: Int, entryText: String, heartMood: String) {
        val cleaned = entryText.trim()
        if (cleaned.isEmpty()) return
        journalEntryDao.insertJournalEntry(
            JournalEntryEntity(
                dayNumber = dayNumber,
                entryText = cleaned,
                heartMood = heartMood,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteJournalEntry(id: Int) {
        journalEntryDao.deleteJournalEntry(id)
    }

    suspend fun saveMonthlyJournalResponse(monthNumber: Int, entryKey: String, content: String) {
        monthlyJournalResponseDao.upsertResponse(
            MonthlyJournalResponseEntity(
                monthNumber = monthNumber,
                entryKey = entryKey,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun buildExportableJournalText(): String {
        val entries = journalEntryDao.observeAllJournalEntries().first()
        val affirmations = affirmationDao.observeAllAffirmations().first().associateBy { it.dayNumber }
        val monthlyResponses = monthlyJournalResponseDao.observeAllResponses().first()
        val dateFormat = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
        val monthNames = listOf(
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        return buildString {
            appendLine("SACRED STEPS TO REDEMPTION — PRIVATE JOURNAL")
            appendLine("Exported on ${dateFormat.format(Date())}")
            appendLine("==================================================")
            appendLine()

            if (entries.isEmpty() && monthlyResponses.none { it.content.isNotBlank() }) {
                appendLine("Your journal pages are still quiet and waiting for your first words.")
            } else {
                if (entries.isNotEmpty()) {
                    appendLine("DAILY REFLECTIONS")
                    appendLine("-----------------")
                    entries.forEach { entry ->
                        val title = affirmations[entry.dayNumber]?.title ?: "Day ${entry.dayNumber}"
                        appendLine("Day ${entry.dayNumber} · $title")
                        if (entry.heartMood.isNotBlank()) {
                            appendLine("Heart Check-In: ${entry.heartMood}")
                        }
                        appendLine("Written: ${dateFormat.format(Date(entry.timestamp))}")
                        appendLine(entry.entryText)
                        appendLine()
                    }
                }

                val nonEmptyMonthly = monthlyResponses.filter { it.content.isNotBlank() }
                if (nonEmptyMonthly.isNotEmpty()) {
                    appendLine("MONTHLY TRANSFORMATION JOURNAL NOTES")
                    appendLine("------------------------------------")
                    nonEmptyMonthly.forEach { note ->
                        val monthLabel = monthNames.getOrElse(note.monthNumber) { "Month ${note.monthNumber}" }
                        appendLine("[$monthLabel] ${note.entryKey}")
                        appendLine(note.content)
                        appendLine()
                    }
                }
            }
        }
    }

    private fun JournalEntryEntity.toDomain(): JournalEntry {
        return JournalEntry(
            id = id,
            dayNumber = dayNumber,
            entryText = entryText,
            heartMood = heartMood,
            timestamp = timestamp
        )
    }
}
