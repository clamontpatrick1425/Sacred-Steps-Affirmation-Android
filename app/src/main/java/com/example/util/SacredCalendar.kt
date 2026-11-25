package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SacredDateInfo(
    val year: Int,
    val monthNumber: Int, // 1..12
    val monthName: String,
    val monthShortName: String,
    val dayOfMonth: Int, // 1..31
    val dayOfYear365: Int, // 1..365 aligned with 365-day library
    val weekNumber: Int, // 1..52
    val weekdayFull: String, // e.g., "Tuesday"
    val weekdayShort: String // e.g., "Tue"
)

/**
 * Maps real-world calendar dates (in the user's local timezone and locale)
 * onto the 365-day devotional calendar, 12 monthly orientation & 365 daily podcasts, and 12 monthly journals.
 */
object SacredCalendar {

    private val monthLengths = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val monthShortNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun today(now: Calendar = Calendar.getInstance()): SacredDateInfo {
        val year = now.get(Calendar.YEAR)
        val monthNumber = now.get(Calendar.MONTH) + 1
        val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
        val dayOfYear365 = toDayOfYear365(monthNumber, dayOfMonth)
        val weekNumber = ((dayOfYear365 - 1) / 7 + 1).coerceIn(1, 52)

        val weekdayFull = SimpleDateFormat("EEEE", Locale.getDefault()).format(now.time)
        val weekdayShort = SimpleDateFormat("EEE", Locale.getDefault()).format(now.time)

        return SacredDateInfo(
            year = year,
            monthNumber = monthNumber,
            monthName = monthNames[monthNumber - 1],
            monthShortName = monthShortNames[monthNumber - 1],
            dayOfMonth = dayOfMonth,
            dayOfYear365 = dayOfYear365,
            weekNumber = weekNumber,
            weekdayFull = weekdayFull,
            weekdayShort = weekdayShort
        )
    }

    fun toDayOfYear365(monthNumber: Int, dayOfMonth: Int): Int {
        val safeMonth = monthNumber.coerceIn(1, 12)
        val safeDay = dayOfMonth.coerceIn(1, monthLengths[safeMonth - 1])
        var sum = 0
        for (m in 0 until safeMonth - 1) {
            sum += monthLengths[m]
        }
        return (sum + safeDay).coerceIn(1, 365)
    }

    fun fromDayOfYear365(
        dayNumber: Int,
        year: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): SacredDateInfo {
        val clamped = dayNumber.coerceIn(1, 365)
        var remaining = clamped
        var monthIdx = 0
        while (monthIdx < 11 && remaining > monthLengths[monthIdx]) {
            remaining -= monthLengths[monthIdx]
            monthIdx++
        }
        val monthNumber = monthIdx + 1
        val dayOfMonth = remaining.coerceIn(1, monthLengths[monthIdx])
        val weekNumber = ((clamped - 1) / 7 + 1).coerceIn(1, 52)

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIdx)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        val weekdayFull = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
        val weekdayShort = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)

        return SacredDateInfo(
            year = year,
            monthNumber = monthNumber,
            monthName = monthNames[monthIdx],
            monthShortName = monthShortNames[monthIdx],
            dayOfMonth = dayOfMonth,
            dayOfYear365 = clamped,
            weekNumber = weekNumber,
            weekdayFull = weekdayFull,
            weekdayShort = weekdayShort
        )
    }

    fun weekdayForMonthDay(
        monthNumber: Int,
        dayOfMonth: Int,
        year: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): String {
        val safeMonthIdx = (monthNumber.coerceIn(1, 12)) - 1
        val safeDay = dayOfMonth.coerceIn(1, monthLengths[safeMonthIdx])
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, safeMonthIdx)
            set(Calendar.DAY_OF_MONTH, safeDay)
        }
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
    }

    /**
     * Keeps Days 1–7 and the current calendar week unlocked so end users opening the app
     * on any date immediately have today's devotional and current week accessible.
     */
    fun isDayInFreeWindow(dayNumber: Int, todayInfo: SacredDateInfo = today()): Boolean {
        if (dayNumber in 1..7) return true
        val dayWeek = ((dayNumber.coerceIn(1, 365) - 1) / 7 + 1).coerceIn(1, 52)
        return dayWeek == todayInfo.weekNumber
    }
}
