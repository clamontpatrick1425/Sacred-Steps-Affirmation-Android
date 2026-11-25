package com.example.podcast

/**
 * Transforms a WeeklyPodcastEpisode (extracted from the 52 Weekly Podcast Input Blocks PDF)
 * into a structured, alternating-voice dialogue script for Murf.ai synthesis.
 *
 * Guarantees strict alternation between the two Murf voices on every consecutive turn
 * and keeps each turn well below Murf's 3,000-character per-request limit.
 */
object PodcastScriptBuilder {

    const val MAX_MURF_TURN_CHARS = 2800

    fun buildDialogueTurns(
        episode: WeeklyPodcastEpisode,
        leadHost: PodcastHost = PodcastHost.GRACE
    ): List<PodcastDialogueTurn> {
        val rawSegments = mutableListOf<Triple<String, String, Int?>>()

        // Turn 0: Welcome & Theme
        rawSegments += Triple(
            "Welcome · Week ${episode.weekNumber}",
            "Welcome back to Sacred Steps to Redemption — a path to recovery, a life in grace. " +
                "This is Week ${episode.weekNumber} of 52, walking through ${episode.monthTheme}. " +
                "Our theme together this week is ${episode.weekTitle}. " +
                "Take a slow breath right where you are. Nothing needs to be proven in this moment.",
            null
        )

        // Turn 1: Weekly Introduction from PDF
        rawSegments += Triple(
            "Weekly Introduction",
            episode.weeklyIntroduction,
            null
        )

        // Turns 2..8: Days 1 through 7 walking through the Sacred S.T.E.P. rhythm
        val dayOpeners = listOf(
            "Let's begin the week anchored in Scripture.",
            "Carrying that forward into day two of our week,",
            "As we step into the middle of the week,",
            "Here at the heart of the week,",
            "Moving into day five,",
            "As we near the close of the week,",
            "And to complete our seven days together,"
        )

        episode.days.forEachIndexed { idx, day ->
            val opener = dayOpeners.getOrElse(idx) { "On day ${day.dayInWeek}," }
            val spoken = buildString {
                append("$opener Day ${day.bookDay} anchors us in God's Word: ${day.scripture}. ")
                append("Here is the truth that frees: ${day.truth} ")
                append("Speak this affirmation out loud with me: ${day.affirmation} ")
                append("And your one small sacred step: ${day.sacredStep}")
            }
            rawSegments += Triple(
                "Day ${day.dayInWeek} · Book Day ${day.bookDay}",
                spoken,
                day.bookDay
            )
        }

        // Turn 9: Weekly Reflection
        rawSegments += Triple(
            "Weekly Reflection",
            "Let's pause and gather what God has been building in you this week. ${episode.weeklyReflection}",
            null
        )

        // Turn 10: Reflection Questions
        if (episode.reflectionQuestions.isNotEmpty()) {
            val questionsSpoken = episode.reflectionQuestions
                .mapIndexed { i, q -> "Question ${i + 1}: $q" }
                .joinToString(" ")
            rawSegments += Triple(
                "Reflection Questions",
                "When you have a quiet minute with your journal, sit gently with these reflection questions. $questionsSpoken",
                null
            )
        }

        // Turn 11: Next Week Preview & Closing Blessing
        rawSegments += Triple(
            "Next Week & Blessing",
            "Looking ahead, next week we walk into ${episode.nextWeekTitle}. " +
                "Remember, grace meets you one honest step at a time. " +
                "We are right here with you. Take the next step.",
            null
        )

        // Strictly alternate voices on every consecutive turn
        return rawSegments.mapIndexed { index, (label, text, dayRef) ->
            val host = if (index % 2 == 0) leadHost else leadHost.alternate()
            PodcastDialogueTurn(
                index = index,
                sectionLabel = label,
                host = host,
                voiceId = host.murfVoiceId,
                spokenText = sanitizeForSpeech(text),
                dayReference = dayRef
            )
        }
    }

    private fun sanitizeForSpeech(raw: String): String {
        val normalized = raw
            .replace("\uFFFD", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (normalized.length <= MAX_MURF_TURN_CHARS) {
            normalized
        } else {
            val cut = normalized.substring(0, MAX_MURF_TURN_CHARS)
            val lastSentence = cut.lastIndexOf('.')
            if (lastSentence > MAX_MURF_TURN_CHARS / 2) {
                cut.substring(0, lastSentence + 1)
            } else {
                cut.trimEnd() + "."
            }
        }
    }
}
