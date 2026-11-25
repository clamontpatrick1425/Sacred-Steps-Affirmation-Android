package com.example.podcast

/**
 * Transforms any PlayablePodcastTrack from SacredStepsAudioManifest_v1 (12 Monthly Orientation
 * Podcasts under 10 minutes + 365 Daily Podcasts at 03:30) into a warm, peer-to-peer,
 * alternating-voice dialogue script for Murf.ai Gen2 synthesis.
 *
 * Uses natural co-host handoffs, spoken Scripture formatting, gentle Murf [pause Xs] breathing
 * beats, and chapter-aware emotional styles to keep every episode human and conversational.
 */
object PodcastScriptBuilder {

    const val MAX_MURF_TURN_CHARS = 2800

    private data class RawTurnSpec(
        val sectionLabel: String,
        val timestampLabel: String,
        val isReflective: Boolean,
        val variation: Int,
        val dayReference: Int?,
        val buildText: (speaker: PodcastHost, partner: PodcastHost) -> String
    )

    fun buildDialogueTurns(
        track: PlayablePodcastTrack,
        leadHost: PodcastHost = PodcastHost.GRACE
    ): List<PodcastDialogueTurn> {
        val specs = when (track) {
            is PlayablePodcastTrack.MonthlyOrientation -> buildMonthlyOrientationSpecs(track.playlist)
            is PlayablePodcastTrack.DailyEpisode -> buildDailyEpisodeSpecs(track.playlist, track.daily)
        }

        // Strictly alternate voices on every consecutive chapter
        return specs.mapIndexed { index, spec ->
            val host = if (index % 2 == 0) leadHost else leadHost.alternate()
            val partner = host.alternate()
            val style = if (spec.isReflective) host.reflectiveStyle else host.conversationalStyle
            val rate = if (spec.isReflective) (host.defaultRate - 2).coerceAtLeast(-15) else host.defaultRate
            PodcastDialogueTurn(
                index = index,
                sectionLabel = spec.sectionLabel,
                timestampLabel = spec.timestampLabel,
                host = host,
                voiceId = host.murfVoiceId,
                spokenText = sanitizeForSpeech(spec.buildText(host, partner)),
                style = style,
                variation = spec.variation,
                rate = rate,
                pitch = host.defaultPitch,
                dayReference = spec.dayReference
            )
        }
    }

    private fun buildMonthlyOrientationSpecs(
        playlist: MonthlyAudioPlaylist
    ): List<RawTurnSpec> {
        val orientation = playlist.monthlyOrientationPodcast
        val markers = orientation.chapterMarkers
        val cleanThemeTitle = cleanTheme(playlist.themeTitle)
        val anchorScripture = formatScriptureForSpeech(playlist.themeScripture)

        fun markerTitle(index: Int, fallback: String): String =
            markers.getOrNull(index)?.title?.takeIf { it.isNotBlank() } ?: fallback

        fun markerTime(index: Int, fallback: String): String =
            markers.getOrNull(index)?.time?.takeIf { it.isNotBlank() } ?: fallback

        val weeksRoadmapSpoken = playlist.weeksOverview.mapIndexed { idx, wk ->
            val cleanWeekTitle = naturalizeProse(wk.title)
            when (idx) {
                0 -> "We open in Week ${wk.weekNumber} with $cleanWeekTitle"
                playlist.weeksOverview.lastIndex -> "and we bring the month home in Week ${wk.weekNumber} with $cleanWeekTitle"
                else -> "then in Week ${wk.weekNumber} we step into $cleanWeekTitle"
            }
        }.joinToString(". [pause 0.5s] ")

        return listOf(
            // Chapter 0 (00:00): The Real Struggle & The Truth
            RawTurnSpec(
                sectionLabel = markerTitle(0, "The Real Struggle & The Truth"),
                timestampLabel = markerTime(0, "00:00"),
                isReflective = false,
                variation = 5,
                dayReference = null
            ) { speaker, partner ->
                "Hey friend, welcome to ${playlist.monthName} on Sacred Steps to Redemption... " +
                    "a path to recovery, and a life in grace. [pause 0.6s] " +
                    "I'm ${speaker.displayName}, here with ${partner.displayName}, " +
                    "and for the next ${playlist.daysCount} days we're walking right beside you through " +
                    "$cleanThemeTitle. [pause 0.7s] " +
                    "Let's be real for a second... stepping into a new month can bring a lot of noise. " +
                    "Old labels, quiet pressure, and the feeling that you have to hold it all together on your own. " +
                    "[pause 0.6s] You don't. Take one slow breath right now, drop your shoulders, " +
                    "and remember that grace meets you right where you actually are."
            },

            // Chapter 1 (02:00): Anchor Scripture & Spiritual Reframe
            RawTurnSpec(
                sectionLabel = markerTitle(1, "Anchor Scripture & Spiritual Reframe"),
                timestampLabel = markerTime(1, "02:00"),
                isReflective = true,
                variation = 4,
                dayReference = null
            ) { _, partner ->
                "I'm so glad you said that, ${partner.displayName}. [pause 0.5s] " +
                    "Because every shift in our thinking starts with anchoring ourselves in what God actually says. " +
                    "[pause 0.6s] Our anchor Scripture for ${playlist.monthName} is $anchorScripture. [pause 0.8s] " +
                    "Shame always tries to make you believe your worst day defines your whole future. " +
                    "God's Word flips that script completely. [pause 0.6s] " +
                    "All month long, whenever fear or doubt gets loud, we're coming back to $anchorScripture " +
                    "to dismantle the lie and stand on steady ground."
            },

            // Chapter 2 (04:30): The Transformation Roadmap
            RawTurnSpec(
                sectionLabel = markerTitle(2, "The Transformation Roadmap"),
                timestampLabel = markerTime(2, "04:30"),
                isReflective = false,
                variation = 5,
                dayReference = null
            ) { _, partner ->
                "Here's how we're going to walk through all ${playlist.daysCount} days of ${playlist.monthName} together, " +
                    "${partner.displayName}. [pause 0.5s] " +
                    "$weeksRoadmapSpoken. [pause 0.7s] " +
                    "Every single morning you'll have a short three-and-a-half-minute daily podcast waiting for you — " +
                    "simple, honest, and easy to carry into your real day."
            },

            // Chapter 3 (07:30): Monthly Dedication Prayer & Sacred S.T.E.P. Habit
            RawTurnSpec(
                sectionLabel = markerTitle(3, "Monthly Dedication Prayer & Habit"),
                timestampLabel = markerTime(3, "07:30"),
                isReflective = true,
                variation = 4,
                dayReference = null
            ) { _, _ ->
                "Before we jump into Day One of ${playlist.monthName}, let's build one simple habit together: " +
                    "the Sacred Step rhythm... Scripture, Truth, Embrace, and Practice. [pause 0.7s] " +
                    "Let's pray. [pause 0.6s] " +
                    "Father, thank You for bringing us to ${playlist.monthName}. " +
                    "Quiet every lie that tries to pull us backward, and anchor our hearts in $anchorScripture. " +
                    "Give us steady courage for one honest step at a time. Amen. [pause 0.8s] " +
                    "We're right here with you. Take the next step."
            }
        )
    }

    private fun buildDailyEpisodeSpecs(
        playlist: MonthlyAudioPlaylist,
        daily: DailyPodcastTrack
    ): List<RawTurnSpec> {
        val step = daily.sacredStep
        val spokenScripture = formatScriptureForSpeech(step.scriptureReference)
        val spokenTruth = naturalizeProse(step.truth)
        val spokenAffirmation = naturalizeProse(step.embraceAffirmation)
        val spokenPractice = naturalizeProse(step.practiceAction)
        val spokenPrayer = naturalizeProse(step.closingPrayer)
        val cleanWeekTitle = naturalizeProse(daily.weekTitle)
        val cleanEpisodeTitle = naturalizeProse(daily.episodeTitle)
        val hookLead = selectDailyHook(daily.dayOfYear)

        return listOf(
            // Chapter 0 (00:00 - 00:45): Conversational Hook & Real Struggle
            RawTurnSpec(
                sectionLabel = "Hook & Real Struggle",
                timestampLabel = "00:00",
                isReflective = false,
                variation = 5,
                dayReference = daily.dayOfYear
            ) { speaker, partner ->
                "$hookLead [pause 0.5s] " +
                    "It's ${daily.dateString}, Day ${daily.dayOfYear} of Sacred Steps to Redemption — " +
                    "I'm ${speaker.displayName}, here with ${partner.displayName}. [pause 0.5s] " +
                    "This week in ${playlist.monthName}, we're walking through $cleanWeekTitle, " +
                    "and today's focus is $cleanEpisodeTitle. [pause 0.6s] " +
                    "Whatever feels heavy or noisy right now, you don't have to fake strength for the next three minutes. " +
                    "Just breathe and meet God right here."
            },

            // Chapter 1 (00:45 - 01:45): Scripture & Truth
            RawTurnSpec(
                sectionLabel = "Scripture & Truth",
                timestampLabel = "00:45",
                isReflective = false,
                variation = 4,
                dayReference = daily.dayOfYear
            ) { _, partner ->
                "Thanks, ${partner.displayName}. [pause 0.4s] " +
                    "Let's anchor today in God's Word from $spokenScripture. [pause 0.8s] " +
                    "Hear the truth that dismantles the lie today... [pause 0.4s] $spokenTruth [pause 0.7s] " +
                    "Let that sink in for a second. You don't have to earn what grace has already spoken over your life."
            },

            // Chapter 2 (01:45 - 02:30): Affirmation & Breath Reset
            RawTurnSpec(
                sectionLabel = "Affirmation & Breath Reset",
                timestampLabel = "01:45",
                isReflective = true,
                variation = 4,
                dayReference = daily.dayOfYear
            ) { _, _ ->
                "Now take one slow breath in... [pause 0.9s] and let it out gently. [pause 0.7s] " +
                    "Say this out loud with me right where you are... [pause 0.5s] " +
                    "$spokenAffirmation [pause 1.0s] " +
                    "Say it one more time in your heart, and let truth speak louder than shame today."
            },

            // Chapter 3 (02:30 - 03:30): Practice, Prayer & Sign-Off
            RawTurnSpec(
                sectionLabel = "Practice, Prayer & Sign-Off",
                timestampLabel = "02:30",
                isReflective = true,
                variation = 4,
                dayReference = daily.dayOfYear
            ) { _, _ ->
                "Here is your one small practice to carry into your day: [pause 0.4s] $spokenPractice [pause 0.8s] " +
                    "Let's close in a quick prayer together. [pause 0.5s] " +
                    "$spokenPrayer [pause 0.8s] " +
                    "We're right here with you. Take the next step."
            }
        )
    }

    private fun selectDailyHook(dayOfYear: Int): String = when (dayOfYear % 6) {
        0 -> "Hey friend, glad you showed up today."
        1 -> "Hey, welcome back — take a second to slow your pace."
        2 -> "Good to be with you today, friend."
        3 -> "Hey there, let's take three honest minutes together."
        4 -> "Welcome back to your daily reset in grace."
        else -> "Hey friend, pause the noise for just a few minutes with us."
    }

    /**
     * Converts written Bible citations into natural spoken English so TTS never reads
     * "2 Corinthians 5:17 (KJV)" robotically as "two Corinthians five colon seventeen K J V".
     */
    internal fun formatScriptureForSpeech(raw: String): String {
        var text = naturalizeProse(raw)
        // Remove parenthetical translation abbreviations like "(KJV)" or "KJV"
        text = text.replace(Regex("\\s*\\(KJV\\)", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("\\bKJV\\b", RegexOption.IGNORE_CASE), "")

        // Expand numbered Bible books ("1 Peter" -> "First Peter", "2 Corinthians" -> "Second Corinthians", etc.)
        val numberedBooks = listOf(
            "Samuel", "Kings", "Chronicles", "Corinthians",
            "Thessalonians", "Timothy", "Peter", "John"
        )
        for (book in numberedBooks) {
            text = text.replace(Regex("\\b1\\s+$book\\b"), "First $book")
            text = text.replace(Regex("\\b2\\s+$book\\b"), "Second $book")
            text = text.replace(Regex("\\b3\\s+$book\\b"), "Third $book")
        }

        // Convert "chapter:start-end" -> "chapter X, verses Y through Z"
        text = text.replace(Regex("(\\d+):(\\d+)[–\\-](\\d+)")) { m ->
            "chapter ${m.groupValues[1]}, verses ${m.groupValues[2]} through ${m.groupValues[3]}"
        }
        // Convert "chapter:verse" -> "chapter X, verse Y"
        text = text.replace(Regex("(\\d+):(\\d+)")) { m ->
            "chapter ${m.groupValues[1]}, verse ${m.groupValues[2]}"
        }

        return text.replace(Regex("\\s+"), " ").trim()
    }

    private fun cleanTheme(raw: String): String {
        return naturalizeProse(raw)
            .replace("—", ", ")
            .replace("–", ", ")
            .replace(":", ", ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun naturalizeProse(raw: String): String {
        return raw
            .replace("\uFFFD", "'")
            .replace("™", "")
            .replace(Regex("\\bGods\\b"), "God's")
            .replace("—", " — ")
            .replace("–", " — ")
            .replace("...", "... ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun sanitizeForSpeech(raw: String): String {
        val normalized = raw
            .replace("\uFFFD", "'")
            .replace("™", "")
            .replace(Regex("\\s+"), " ")
            .replace(" .", ".")
            .replace("..", ".")
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
