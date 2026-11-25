package com.example.journal

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MonthlyJournalRepository(
    private val context: Context
) {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Volatile
    private var cachedJournals: List<MonthlyTransformationJournal>? = null

    suspend fun loadAllJournals(): List<MonthlyTransformationJournal> = withContext(Dispatchers.IO) {
        cachedJournals?.let { return@withContext it }

        val json = context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
        val adapter = moshi.adapter(MonthlyJournalCatalog::class.java)
        val parsed = adapter.fromJson(json)?.journals.orEmpty().sortedBy { it.monthNumber }
        cachedJournals = parsed
        parsed
    }

    companion object {
        const val ASSET_FILE_NAME = "monthly_journals.json"
    }
}
