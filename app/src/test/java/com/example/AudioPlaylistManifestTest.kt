package com.example

import com.example.podcast.AudioPlaylistManifest
import com.example.podcast.PlayablePodcastTrack
import com.example.podcast.PodcastHost
import com.example.podcast.PodcastRepository
import com.example.podcast.PodcastScriptBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AudioPlaylistManifestTest {

    private fun loadManifestFromAssets(): AudioPlaylistManifest {
        val candidates = listOf(
            File("src/main/assets/${PodcastRepository.ASSET_FILE_NAME}"),
            File("app/src/main/assets/${PodcastRepository.ASSET_FILE_NAME}")
        )
        val assetFile = candidates.firstOrNull { it.exists() }
            ?: error("Could not locate ${PodcastRepository.ASSET_FILE_NAME} in ${candidates.map { it.absolutePath }}")

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(AudioPlaylistManifest::class.java)
        return requireNotNull(adapter.fromJson(assetFile.readText())) {
            "Failed to parse AudioPlaylistManifest"
        }
    }

    @Test
    fun manifestContains12MonthlyOrientationsAnd365DailyPodcastsWithExactCalendarDays() {
        val manifest = loadManifestFromAssets()

        assertEquals("SacredStepsAudioManifest_v1", manifest.manifestInfo.schemaName)
        assertEquals(12, manifest.manifestInfo.totals.totalMonths)
        assertEquals(12, manifest.manifestInfo.totals.totalMonthlyPodcasts)
        assertEquals(365, manifest.manifestInfo.totals.totalDailyPodcasts)
        assertEquals(377, manifest.manifestInfo.totals.totalAudioTracks)
        assertEquals(12, manifest.monthlyPlaylists.size)

        val expectedDaysByMonth = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val allDailyTracks = mutableListOf<Int>()

        manifest.monthlyPlaylists.forEachIndexed { index, playlist ->
            val expectedMonth = index + 1
            val expectedDays = expectedDaysByMonth[index]
            assertEquals(expectedMonth, playlist.monthNumber)
            assertEquals(expectedDays, playlist.daysCount)
            assertEquals(expectedDays, playlist.dailyPodcasts.size)

            val orientation = playlist.monthlyOrientationPodcast
            assertTrue(orientation.trackId.startsWith("POD-MONTH-"))
            assertTrue(orientation.isUnder10Minutes)
            assertTrue(
                "Monthly orientation ${orientation.trackId} must be under 10 minutes",
                orientation.durationSeconds < 600
            )
            assertEquals(4, orientation.chapterMarkers.size)

            playlist.dailyPodcasts.forEachIndexed { dayIdx, daily ->
                assertEquals(dayIdx + 1, daily.dayOfMonth)
                assertEquals("03:30", daily.durationFormatted)
                assertEquals(210, daily.durationSeconds)
                assertTrue(daily.trackId.startsWith("POD-DAY-"))
                assertTrue(daily.sacredStep.scriptureReference.isNotBlank())
                assertTrue(daily.sacredStep.truth.isNotBlank())
                assertTrue(daily.sacredStep.embraceAffirmation.isNotBlank())
                assertTrue(daily.sacredStep.practiceAction.isNotBlank())
                assertTrue(daily.sacredStep.closingPrayer.isNotBlank())
                allDailyTracks += daily.dayOfYear
            }
        }

        assertEquals((1..365).toList(), allDailyTracks)
    }

    @Test
    fun eachPodcastHasSingleSoloPresenterAndRotatesAcrossTopFiveMurfVoices() {
        assertEquals(5, PodcastHost.entries.size)
        assertEquals(
            listOf("en-US-natalie", "en-US-terrell", "en-US-julia", "en-US-miles", "en-US-amara"),
            PodcastHost.entries.map { it.murfVoiceId }
        )

        val manifest = loadManifestFromAssets()

        // Monthly orientations rotate across all 5 voices from month to month
        val firstSixMonthPresenters = manifest.monthlyPlaylists.take(6).map { playlist ->
            val track = PlayablePodcastTrack.MonthlyOrientation(playlist)
            val turns = PodcastScriptBuilder.buildDialogueTurns(track)
            assertEquals(4, turns.size)
            val expectedHost = PodcastHost.forMonth(playlist.monthNumber)
            // Every chapter in a podcast uses the exact same single presenter
            turns.forEach { turn ->
                assertEquals(expectedHost, turn.host)
                assertEquals(expectedHost.murfVoiceId, turn.voiceId)
                assertTrue(turn.spokenText.isNotBlank())
                assertTrue(turn.spokenText.length <= PodcastScriptBuilder.MAX_MURF_TURN_CHARS)
                assertTrue(turn.variation in 4..5)
                assertTrue(turn.rate < 0)
                assertFalse(turn.spokenText.contains("(KJV)"))
                assertFalse(Regex("\\bGods\\b").containsMatchIn(turn.spokenText))
            }
            expectedHost
        }

        assertEquals(PodcastHost.entries, firstSixMonthPresenters.take(5))
        assertEquals(PodcastHost.NATALIE, firstSixMonthPresenters[5])

        // Daily podcasts rotate across all 5 voices from day to day while staying solo within each episode
        val january = manifest.monthlyPlaylists.first()
        val firstSixDayPresenters = january.dailyPodcasts.take(6).map { daily ->
            val track = PlayablePodcastTrack.DailyEpisode(january, daily)
            val turns = PodcastScriptBuilder.buildDialogueTurns(track)
            assertEquals(4, turns.size)
            assertEquals(listOf("00:00", "00:45", "01:45", "02:30"), turns.map { it.timestampLabel })

            val expectedHost = PodcastHost.forDayOfYear(daily.dayOfYear)
            turns.forEach { turn ->
                assertEquals(expectedHost, turn.host)
                assertEquals(expectedHost.murfVoiceId, turn.voiceId)
                assertNotNull(turn.dayReference)
                assertEquals(daily.dayOfYear, turn.dayReference)
                assertTrue(turn.spokenText.contains("[pause "))
                assertFalse(turn.spokenText.contains("(KJV)"))
            }
            expectedHost
        }

        assertEquals(PodcastHost.entries, firstSixDayPresenters.take(5))
        assertEquals(PodcastHost.NATALIE, firstSixDayPresenters[5])
    }

    @Test
    fun scriptureCitationFormattingReadsNaturallyAloud() {
        assertEquals(
            "First Peter chapter 2, verse 9",
            PodcastScriptBuilder.formatScriptureForSpeech("1 Peter 2:9 (KJV)")
        )
        assertEquals(
            "Second Corinthians chapter 5, verses 17 through 21",
            PodcastScriptBuilder.formatScriptureForSpeech("2 Corinthians 5:17-21 (KJV)")
        )
        assertEquals(
            "Isaiah chapter 43, verse 1",
            PodcastScriptBuilder.formatScriptureForSpeech("Isaiah 43:1 (KJV)")
        )
    }
}
