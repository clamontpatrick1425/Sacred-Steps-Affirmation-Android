package com.example.data.repository

import com.example.data.local.AffirmationDao
import com.example.data.local.JournalEntryDao
import com.example.data.local.JournalEntryEntity
import com.example.data.local.WorkbookNoteDao
import com.example.data.local.WorkbookNoteEntity
import com.example.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalRepository(
    private val journalEntryDao: JournalEntryDao,
    private val workbookNoteDao: WorkbookNoteDao,
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

    val workbookNotesMap: Flow<Map<String, String>> = workbookNoteDao.observeAllWorkbookNotes().map { list ->
        list.associate { "${it.moduleId}:${it.promptKey}" to it.content }
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

    suspend fun saveWorkbookNote(moduleId: String, promptKey: String, content: String) {
        workbookNoteDao.upsertWorkbookNote(
            WorkbookNoteEntity(
                moduleId = moduleId,
                promptKey = promptKey,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun buildExportableJournalText(): String {
        val entries = journalEntryDao.observeAllJournalEntries().first()
        val affirmations = affirmationDao.observeAllAffirmations().first().associateBy { it.dayNumber }
        val workbookNotes = workbookNoteDao.observeAllWorkbookNotes().first()
        val dateFormat = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())

        return buildString {
            appendLine("SACRED STEPS TO REDEMPTION — PRIVATE JOURNAL")
            appendLine("Exported on ${dateFormat.format(Date())}")
            appendLine("==================================================")
            appendLine()

            if (entries.isEmpty() && workbookNotes.none { it.content.isNotBlank() }) {
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

                val nonEmptyNotes = workbookNotes.filter { it.content.isNotBlank() }
                if (nonEmptyNotes.isNotEmpty()) {
                    appendLine("COMPANION WORKBOOK NOTES")
                    appendLine("------------------------")
                    nonEmptyNotes.forEach { note ->
                        appendLine("[${note.moduleId}] ${note.promptKey}")
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
