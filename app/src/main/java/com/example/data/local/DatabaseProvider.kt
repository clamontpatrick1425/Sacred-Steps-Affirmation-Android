package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Provides the singleton Room database instance and pre-populates the initial
 * 7 free devotional days on creation via [RoomDatabase.Callback], followed by
 * the full 365-day offline library.
 */
object DatabaseProvider {

    private const val DATABASE_NAME = "sacred_steps.db"

    @Volatile
    private var instance: SacredStepsDatabase? = null

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getDatabase(context: Context): SacredStepsDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { instance = it }
        }
    }

    private fun buildDatabase(appContext: Context): SacredStepsDatabase {
        return Room.databaseBuilder(
            appContext,
            SacredStepsDatabase::class.java,
            DATABASE_NAME
        )
            .addCallback(PrepopulateCallback())
            .build()
    }

    suspend fun ensureFullLibrarySeeded(database: SacredStepsDatabase) {
        val affirmationDao = database.affirmationDao()
        if (affirmationDao.getAffirmationCount() < 7) {
            affirmationDao.insertAffirmations(AffirmationSeedData.buildFirstSevenDays())
        }
        if (affirmationDao.getAffirmationCount() < 365) {
            affirmationDao.insertAffirmations(AffirmationSeedData.buildAll365Days())
        }
    }

    private class PrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            ioScope.launch {
                instance?.let { database ->
                    database.affirmationDao().insertAffirmations(
                        AffirmationSeedData.buildFirstSevenDays()
                    )
                    database.affirmationDao().insertAffirmations(
                        AffirmationSeedData.buildAll365Days()
                    )
                }
            }
        }
    }
}
