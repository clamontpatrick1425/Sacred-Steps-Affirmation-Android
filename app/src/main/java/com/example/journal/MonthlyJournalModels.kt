package com.example.journal

data class MonthlyJournalCatalog(
    val journals: List<MonthlyTransformationJournal> = emptyList()
)

data class MonthlyTransformationJournal(
    val monthNumber: Int,
    val monthName: String,
    val themeTitle: String,
    val subtitle: String,
    val welcomeLetter: String,
    val themeIntro: String,
    val themeScripture: String,
    val openingPrayer: String,
    val weeks: List<MonthlyJournalWeek> = emptyList(),
    val days: List<MonthlyJournalDay> = emptyList(),
    val endOfMonthTitle: String,
    val endOfMonthPrompts: List<EndOfMonthPrompt> = emptyList(),
    val declarationTitle: String,
    val declarationAnchors: List<String> = emptyList(),
    val closingMessage: String,
    val closingPrayer: String
)

data class MonthlyJournalWeek(
    val weekIndex: Int,
    val title: String,
    val intro: String,
    val dayStart: Int,
    val dayEnd: Int,
    val checkInPrompt: String
)

data class MonthlyJournalDay(
    val dayNumber: Int,
    val title: String,
    val scripture: String,
    val whatThisMeans: String,
    val howIApplyToday: String,
    val lieReplaced: String,
    val truthAnchor: String,
    val embraceAffirmation: String,
    val practiceAction: String,
    val reflectionQuestions: List<String> = emptyList(),
    val prayer: String
)

data class EndOfMonthPrompt(
    val key: String,
    val title: String,
    val helper: String
)
