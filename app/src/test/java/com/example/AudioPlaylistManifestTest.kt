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
import org.junit.Assert.assertNotEquals
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
    fun monthlyOrientationScriptStrictlyAlternatesNaturalMurfVoices() {
        val manifest = loadManifestFromAssets()
        val january = manifest.monthlyPlaylists.first()
        val track = PlayablePodcastTrack.MonthlyOrientation(january)

        val turns = PodcastScriptBuilder.buildDialogueTurns(track, leadHost = PodcastHost.GRACE)
        assertEquals(4, turns.size)
        assertEquals("00:00", turns[0].timestampLabel)
        assertEquals("02:00", turns[1].timestampLabel)
        assertEquals("04:30", turns[2].timestampLabel)
        assertEquals("07:30", turns[3].timestampLabel)
        assertEquals(PodcastHost.GRACE, turns.first().host)

        for (i in 1 until turns.size) {
            assertNotEquals(turns[i - 1].host, turns[i].host)
            assertNotEquals(turns[i - 1].voiceId, turns[i].voiceId)
        }
        turns.forEach { turn ->
            assertTrue(turn.spokenText.isNotBlank())
            assertTrue(turn.spokenText.length <= PodcastScriptBuilder.MAX_MURF_TURN_CHARS)
            assertTrue(turn.variation in 4..5)
            assertTrue(turn.rate < 0)
            assertFalse("Spoken text must not contain KJV abbreviation", turn.spokenText.contains("(KJV)"))
            assertFalse("Spoken text should naturalize Gods -> God's", Regex("\\bGods\\b").containsMatchIn(turn.spokenText))
        }
    }

    @Test
    fun dailyPodcastScriptAlternatesVoicesAndSupportsSwappingLeadHost() {
        val manifest = loadManifestFromAssets()
        val january = manifest.monthlyPlaylists.first()
        val day1 = january.dailyPodcasts.first()
        val track = PlayablePodcastTrack.DailyEpisode(january, day1)

        val marcusFirst = PodcastScriptBuilder.buildDialogueTurns(track, leadHost = PodcastHost.MARCUS)
        assertEquals(4, marcusFirst.size)
        assertEquals(PodcastHost.MARCUS, marcusFirst[0].host)
        assertEquals(PodcastHost.GRACE, marcusFirst[1].host)
        assertEquals(PodcastHost.MARCUS, marcusFirst[2].host)
        assertEquals(PodcastHost.GRACE, marcusFirst[3].host)

        assertEquals("00:00", marcusFirst[0].timestampLabel)
        assertEquals("00:45", marcusFirst[1].timestampLabel)
        assertEquals("01:45", marcusFirst[2].timestampLabel)
        assertEquals("02:30", marcusFirst[3].timestampLabel)

        marcusFirst.forEach { turn ->
            assertNotNull(turn.dayReference)
            assertEquals(1, turn.dayReference)
            assertTrue(turn.spokenText.contains("[pause "))
            assertFalse(turn.spokenText.contains("(KJV)"))
        }
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
