package com.sacredsteps.app.audio.model

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudioPlaylistManifest(
    @SerialName("project_title") val projectTitle: String,
    @SerialName("version") val version: String,
    @SerialName("voice_tone") val voiceTone: String,
    @SerialName("manifest_metadata") val metadata: ManifestMetadata,
    @SerialName("monthly_podcasts") val monthlyPodcasts: List<MonthlyPodcast>,
    @SerialName("daily_podcasts") val dailyPodcasts: List<DailyPodcast>
)

@Serializable
data class ManifestMetadata(
    @SerialName("total_months") val totalMonths: Int,
    @SerialName("total_days") val totalDays: Int,
    @SerialName("target_age_group") val targetAgeGroup: String,
    @SerialName("audio_format") val audioFormat: String,
    @SerialName("audio_bitrate") val audioBitrate: String,
    @SerialName("sample_rate") val sampleRate: String
)

@Serializable
data class ChapterMarker(
    @SerialName("title") val title: String,
    @SerialName("start_time_seconds") val startTimeSeconds: Int,
    @SerialName("formatted_time") val formattedTime: String
)

@Serializable
data class AudioStream(
    @SerialName("stream_url") val streamUrl: String,
    @SerialName("fallback_cdn_url") val fallbackCdnUrl: String,
    @SerialName("mime_type") val mimeType: String,
    @SerialName("file_size_bytes") val fileSizeBytes: Long
)

@Serializable
data class MonthlyPodcast(
    @SerialName("month_number") val monthNumber: Int,
    @SerialName("month_name") val monthName: String,
    @SerialName("theme") val theme: String,
    @SerialName("title") val title: String,
    @SerialName("anchor_scripture") val anchorScripture: String,
    @SerialName("episode_type") val episodeType: String,
    @SerialName("target_duration_seconds") val targetDurationSeconds: Int,
    @SerialName("duration_formatted") val durationFormatted: String,
    @SerialName("track_id") val trackId: String,
    @SerialName("artwork_asset") val artworkAsset: String,
    @SerialName("audio_stream") val audioStream: AudioStream,
    @SerialName("chapters") val chapters: List<ChapterMarker> = emptyList(),
    @SerialName("roadmap_weeks") val roadmapWeeks: List<String> = emptyList()
) {
    fun toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(trackId)
            .setUri(audioStream.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist("Sacred Steps to Redemption")
                    .setAlbumTitle("$monthName: $theme")
                    .setDescription(anchorScripture)
                    .setArtworkUri(Uri.parse(artworkAsset))
                    .build()
            )
            .build()
    }
}

@Serializable
data class StepDetails(
    @SerialName("scripture") val scripture: String,
    @SerialName("truth") val truth: String,
    @SerialName("affirmation") val affirmation: String,
    @SerialName("step") val step: String,
    @SerialName("prayer") val prayer: String
)

@Serializable
data class DailyPodcast(
    @SerialName("day_of_year") val dayOfYear: Int,
    @SerialName("month_number") val monthNumber: Int,
    @SerialName("day_of_month") val dayOfMonth: Int,
    @SerialName("date_label") val dateLabel: String,
    @SerialName("week_number") val weekNumber: Int,
    @SerialName("weekly_theme") val weeklyTheme: String,
    @SerialName("title") val title: String,
    @SerialName("scripture_reference") val scriptureReference: String,
    @SerialName("episode_type") val episodeType: String,
    @SerialName("target_duration_seconds") val targetDurationSeconds: Int,
    @SerialName("duration_formatted") val durationFormatted: String,
    @SerialName("track_id") val trackId: String,
    @SerialName("artwork_asset") val artworkAsset: String,
    @SerialName("audio_stream") val audioStream: AudioStream,
    @SerialName("step_details") val stepDetails: StepDetails,
    @SerialName("chapters") val chapters: List<ChapterMarker> = emptyList()
) {
    fun toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(trackId)
            .setUri(audioStream.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist("Sacred Steps: Daily Step")
                    .setAlbumTitle(weeklyTheme)
                    .setDescription(scriptureReference)
                    .setArtworkUri(Uri.parse(artworkAsset))
                    .build()
            )
            .build()
    }
}
