package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.podcast.PodcastHost
import com.example.podcast.PodcastRepository
import com.example.podcast.PodcastScriptBuilder
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WeeklyPodcastTest {

    @Test
    fun all52WeeklyPodcastsLoadCleanlyFromBundledAsset() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PodcastRepository(context)
        val episodes = repository.loadAllEpisodes()

        assertEquals("Expected all 52 weekly podcast episodes", 52, episodes.size)
        assertEquals(1, episodes.first().weekNumber)
        assertEquals("I Am Chosen", episodes.first().weekTitle)
        assertEquals(52, episodes.last().weekNumber)
        assertEquals("Sustained Walking, Daily Freedom", episodes.last().weekTitle)

        episodes.forEachIndexed { index, episode ->
            assertEquals(index + 1, episode.weekNumber)
            assertEquals("Week ${episode.weekNumber} should have 7 days", 7, episode.days.size)
            assertTrue(episode.monthName.isNotBlank())
            assertTrue(episode.weekTitle.isNotBlank())
            assertTrue(episode.weeklyIntroduction.isNotBlank())
            assertTrue(episode.weeklyReflection.isNotBlank())
            assertTrue(episode.reflectionQuestions.isNotEmpty())
            assertTrue(episode.nextWeekTitle.isNotBlank())
        }
    }

    @Test
    fun dialogueScriptStrictlyAlternatesMurfVoicesWithinCharacterLimit() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val episodes = PodcastRepository(context).loadAllEpisodes()

        for (leadHost in listOf(PodcastHost.GRACE, PodcastHost.MARCUS)) {
            episodes.forEach { episode ->
                val turns = PodcastScriptBuilder.buildDialogueTurns(episode, leadHost)
                assertTrue("Expected at least 11 turns for Week ${episode.weekNumber}", turns.size >= 11)
                assertEquals(leadHost, turns.first().host)

                turns.forEachIndexed { idx, turn ->
                    assertEquals(idx, turn.index)
                    assertTrue(
                        "Turn $idx in Week ${episode.weekNumber} exceeded Murf limit",
                        turn.spokenText.length <= PodcastScriptBuilder.MAX_MURF_TURN_CHARS
                    )
                    assertTrue(turn.spokenText.isNotBlank())
                    if (idx > 0) {
                        val previous = turns[idx - 1]
                        assertNotEquals(
                            "Consecutive turns must alternate hosts in Week ${episode.weekNumber}",
                            previous.host,
                            turn.host
                        )
                        assertNotEquals(
                            "Consecutive turns must alternate Murf voice IDs in Week ${episode.weekNumber}",
                            previous.voiceId,
                            turn.voiceId
                        )
                    }
                }
            }
        }
    }
}
