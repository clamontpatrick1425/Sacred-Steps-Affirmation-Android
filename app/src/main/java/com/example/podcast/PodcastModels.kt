package com.example.podcast

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeeklyPodcastCatalog(
    val episodes: List<WeeklyPodcastEpisode>
)

@JsonClass(generateAdapter = true)
data class WeeklyPodcastEpisode(
    val weekNumber: Int,
    val monthName: String,
    val monthTheme: String,
    val weekTitle: String,
    val weeklyIntroduction: String,
    val days: List<PodcastDayInput>,
    val weeklyReflection: String,
    val reflectionQuestions: List<String>,
    val nextWeekTitle: String
)

@JsonClass(generateAdapter = true)
data class PodcastDayInput(
    val dayInWeek: Int,
    val bookDay: Int,
    val scripture: String,
    val truth: String,
    val affirmation: String,
    val sacredStep: String
)

enum class PodcastHost(
    val displayName: String,
    val roleLabel: String,
    val murfVoiceId: String
) {
    GRACE(
        displayName = "Grace",
        roleLabel = "Warm Companion",
        murfVoiceId = "en-US-natalie"
    ),
    MARCUS(
        displayName = "Marcus",
        roleLabel = "Scripture & Practice",
        murfVoiceId = "en-US-marcus"
    );

    fun alternate(): PodcastHost = when (this) {
        GRACE -> MARCUS
        MARCUS -> GRACE
    }
}

data class PodcastDialogueTurn(
    val index: Int,
    val sectionLabel: String,
    val host: PodcastHost,
    val voiceId: String,
    val spokenText: String,
    val dayReference: Int? = null
)

enum class PodcastPlaybackStatus {
    IDLE,
    SYNTHESIZING,
    PLAYING,
    PAUSED,
    ERROR
}
