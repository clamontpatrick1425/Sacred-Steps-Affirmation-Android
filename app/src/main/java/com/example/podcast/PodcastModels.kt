package com.example.podcast

import com.squareup.moshi.Json

/**
 * Data models matching SacredStepsAudioManifest_v1
 * (12 Monthly Orientation Podcasts under 10 minutes + 365 Daily Podcasts = 377 Audio Tracks).
 */
data class AudioPlaylistManifest(
    @Json(name = "manifest_info") val manifestInfo: ManifestInfo,
    @Json(name = "monthly_playlists") val monthlyPlaylists: List<MonthlyAudioPlaylist>
)

data class ManifestInfo(
    @Json(name = "version") val version: String,
    @Json(name = "schema_name") val schemaName: String,
    @Json(name = "app_package") val appPackage: String,
    @Json(name = "app_title") val appTitle: String,
    @Json(name = "author") val author: String,
    @Json(name = "publisher") val publisher: String,
    @Json(name = "target_audience") val targetAudience: String,
    @Json(name = "podcast_tone") val podcastTone: String,
    @Json(name = "specs") val specs: AudioSpecs,
    @Json(name = "totals") val totals: ManifestTotals
)

data class AudioSpecs(
    @Json(name = "audio_format") val audioFormat: String,
    @Json(name = "sample_rate_hz") val sampleRateHz: Int,
    @Json(name = "bitrate_kbps") val bitrateKbps: Int,
    @Json(name = "audio_base_path") val audioBasePath: String,
    @Json(name = "artwork_base_path") val artworkBasePath: String,
    @Json(name = "monthly_podcast_duration_limit_minutes") val monthlyPodcastDurationLimitMinutes: Int,
    @Json(name = "monthly_podcast_target_seconds") val monthlyPodcastTargetSeconds: Int,
    @Json(name = "daily_podcast_target_seconds") val dailyPodcastTargetSeconds: Int,
    @Json(name = "daily_podcast_duration_formatted") val dailyPodcastDurationFormatted: String
)

data class ManifestTotals(
    @Json(name = "total_months") val totalMonths: Int,
    @Json(name = "total_weeks") val totalWeeks: Int,
    @Json(name = "total_days") val totalDays: Int,
    @Json(name = "total_monthly_podcasts") val totalMonthlyPodcasts: Int,
    @Json(name = "total_daily_podcasts") val totalDailyPodcasts: Int,
    @Json(name = "total_audio_tracks") val totalAudioTracks: Int
)

data class MonthlyAudioPlaylist(
    @Json(name = "month_number") val monthNumber: Int,
    @Json(name = "month_name") val monthName: String,
    @Json(name = "days_count") val daysCount: Int,
    @Json(name = "theme_title") val themeTitle: String,
    @Json(name = "theme_scripture") val themeScripture: String,
    @Json(name = "monthly_orientation_podcast") val monthlyOrientationPodcast: MonthlyOrientationPodcast,
    @Json(name = "weeks_overview") val weeksOverview: List<WeekOverviewItem>,
    @Json(name = "daily_podcasts") val dailyPodcasts: List<DailyPodcastTrack>
)

data class MonthlyOrientationPodcast(
    @Json(name = "track_id") val trackId: String,
    @Json(name = "title") val title: String,
    @Json(name = "duration_seconds") val durationSeconds: Int,
    @Json(name = "duration_formatted") val durationFormatted: String,
    @Json(name = "is_under_10_minutes") val isUnder10Minutes: Boolean,
    @Json(name = "max_limit_minutes") val maxLimitMinutes: Int,
    @Json(name = "audio_file") val audioFile: String,
    @Json(name = "artwork_file") val artworkFile: String,
    @Json(name = "chapter_markers") val chapterMarkers: List<AudioChapterMarker>
)

data class AudioChapterMarker(
    @Json(name = "time") val time: String,
    @Json(name = "title") val title: String
)

data class WeekOverviewItem(
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "title") val title: String
)

data class DailyPodcastTrack(
    @Json(name = "day_of_year") val dayOfYear: Int,
    @Json(name = "day_of_month") val dayOfMonth: Int,
    @Json(name = "date_string") val dateString: String,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "week_title") val weekTitle: String,
    @Json(name = "track_id") val trackId: String,
    @Json(name = "episode_title") val episodeTitle: String,
    @Json(name = "duration_seconds") val durationSeconds: Int,
    @Json(name = "duration_formatted") val durationFormatted: String,
    @Json(name = "audio_file") val audioFile: String,
    @Json(name = "artwork_file") val artworkFile: String,
    @Json(name = "sacred_step") val sacredStep: DailyPodcastSacredStep,
    @Json(name = "script_pacing") val scriptPacing: DailyScriptPacing
)

data class DailyPodcastSacredStep(
    @Json(name = "scripture_reference") val scriptureReference: String,
    @Json(name = "truth") val truth: String,
    @Json(name = "embrace_affirmation") val embraceAffirmation: String,
    @Json(name = "practice_action") val practiceAction: String,
    @Json(name = "closing_prayer") val closingPrayer: String
)

data class DailyScriptPacing(
    @Json(name = "hook_intro") val hookIntro: String,
    @Json(name = "scripture_truth") val scriptureTruth: String,
    @Json(name = "affirmation_embrace") val affirmationEmbrace: String,
    @Json(name = "action_prayer_outro") val actionPrayerOutro: String
)

/**
 * Unified playable track model for both Monthly Orientation Podcasts and Daily Podcasts.
 */
sealed interface PlayablePodcastTrack {
    val trackId: String
    val title: String
    val subtitle: String
    val badgeLabel: String
    val durationFormatted: String
    val durationSeconds: Int
    val audioFile: String
    val monthNumber: Int
    val monthName: String
    val isMonthlyOrientation: Boolean

    data class MonthlyOrientation(
        val playlist: MonthlyAudioPlaylist
    ) : PlayablePodcastTrack {
        val orientation: MonthlyOrientationPodcast get() = playlist.monthlyOrientationPodcast
        override val trackId: String get() = orientation.trackId
        override val title: String get() = "${playlist.monthName} Orientation"
        override val subtitle: String get() = playlist.themeTitle
        override val badgeLabel: String
            get() = "${orientation.trackId} · ${orientation.durationFormatted} · UNDER ${orientation.maxLimitMinutes} MIN"
        override val durationFormatted: String get() = orientation.durationFormatted
        override val durationSeconds: Int get() = orientation.durationSeconds
        override val audioFile: String get() = orientation.audioFile
        override val monthNumber: Int get() = playlist.monthNumber
        override val monthName: String get() = playlist.monthName
        override val isMonthlyOrientation: Boolean get() = true
    }

    data class DailyEpisode(
        val playlist: MonthlyAudioPlaylist,
        val daily: DailyPodcastTrack
    ) : PlayablePodcastTrack {
        override val trackId: String get() = daily.trackId
        override val title: String get() = daily.episodeTitle
        override val subtitle: String get() = "${daily.dateString} · ${daily.weekTitle}"
        override val badgeLabel: String
            get() = "${daily.trackId} · DAY ${daily.dayOfYear} OF 365 · ${daily.durationFormatted}"
        override val durationFormatted: String get() = daily.durationFormatted
        override val durationSeconds: Int get() = daily.durationSeconds
        override val audioFile: String get() = daily.audioFile
        override val monthNumber: Int get() = playlist.monthNumber
        override val monthName: String get() = playlist.monthName
        override val isMonthlyOrientation: Boolean get() = false
    }
}

/**
 * Top 5 Murf.ai Gen2 studio voices.
 * Each podcast is presented from start to finish by ONE dedicated presenter,
 * rotating across these 5 voices from episode to episode.
 */
enum class PodcastHost(
    val displayName: String,
    val roleLabel: String,
    val murfVoiceId: String,
    val conversationalStyle: String,
    val reflectiveStyle: String,
    val defaultRate: Int,
    val defaultPitch: Int
) {
    NATALIE(
        displayName = "Natalie",
        roleLabel = "Warm & Conversational",
        murfVoiceId = "en-US-natalie",
        conversationalStyle = "Conversational",
        reflectiveStyle = "Inspirational",
        defaultRate = -4,
        defaultPitch = 0
    ),
    TERRELL(
        displayName = "Terrell",
        roleLabel = "Grounded & Inspirational",
        murfVoiceId = "en-US-terrell",
        conversationalStyle = "Conversational",
        reflectiveStyle = "Calm",
        defaultRate = -5,
        defaultPitch = -2
    ),
    JULIA(
        displayName = "Julia",
        roleLabel = "Calm & Reflective",
        murfVoiceId = "en-US-julia",
        conversationalStyle = "Conversational",
        reflectiveStyle = "Calm",
        defaultRate = -4,
        defaultPitch = 0
    ),
    MILES(
        displayName = "Miles",
        roleLabel = "Encouraging Mentor",
        murfVoiceId = "en-US-miles",
        conversationalStyle = "Conversational",
        reflectiveStyle = "Narration",
        defaultRate = -4,
        defaultPitch = -1
    ),
    AMARA(
        displayName = "Amara",
        roleLabel = "Grace & Clarity",
        murfVoiceId = "en-US-amara",
        conversationalStyle = "Conversational",
        reflectiveStyle = "Calm",
        defaultRate = -4,
        defaultPitch = 0
    );

    fun next(): PodcastHost = entries[(ordinal + 1) % entries.size]

    companion object {
        fun forMonth(monthNumber: Int): PodcastHost {
            val idx = ((monthNumber - 1).coerceAtLeast(0)) % entries.size
            return entries[idx]
        }

        fun forDayOfYear(dayOfYear: Int): PodcastHost {
            val idx = ((dayOfYear - 1).coerceAtLeast(0)) % entries.size
            return entries[idx]
        }

        fun presenterForTrack(track: PlayablePodcastTrack): PodcastHost = when (track) {
            is PlayablePodcastTrack.MonthlyOrientation -> forMonth(track.monthNumber)
            is PlayablePodcastTrack.DailyEpisode -> forDayOfYear(track.daily.dayOfYear)
        }
    }
}

data class PodcastDialogueTurn(
    val index: Int,
    val sectionLabel: String,
    val timestampLabel: String,
    val host: PodcastHost,
    val voiceId: String,
    val spokenText: String,
    val style: String = host.conversationalStyle,
    val variation: Int = 4,
    val rate: Int = host.defaultRate,
    val pitch: Int = host.defaultPitch,
    val dayReference: Int? = null
)

enum class PodcastPlaybackStatus {
    IDLE,
    SYNTHESIZING,
    PLAYING,
    PAUSED,
    ERROR
}
