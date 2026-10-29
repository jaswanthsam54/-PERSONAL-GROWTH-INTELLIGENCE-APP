package com.example.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    private val DISPLAY_DATE_FORMAT = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val DAY_OF_WEEK_FORMAT = SimpleDateFormat("EEE", Locale.getDefault())
    private val FULL_DISPLAY_FORMAT = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    fun todayString(): String {
        return ISO_DATE_FORMAT.format(Date())
    }

    fun formatDate(isoDate: String): String {
        return try {
            val date = ISO_DATE_FORMAT.parse(isoDate) ?: return isoDate
            DISPLAY_DATE_FORMAT.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatFullDate(isoDate: String): String {
        return try {
            val date = ISO_DATE_FORMAT.parse(isoDate) ?: return isoDate
            FULL_DISPLAY_FORMAT.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun getDayOfWeek(isoDate: String): String {
        return try {
            val date = ISO_DATE_FORMAT.parse(isoDate) ?: return ""
            DAY_OF_WEEK_FORMAT.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun addDays(isoDate: String, days: Int): String {
        return try {
            val calendar = Calendar.getInstance()
            val date = ISO_DATE_FORMAT.parse(isoDate) ?: Date()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, days)
            ISO_DATE_FORMAT.format(calendar.time)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun getPastDays(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -count + 1)
        for (i in 0 until count) {
            list.add(ISO_DATE_FORMAT.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun getStartOfWeek(isoDate: String = todayString()): String {
        return try {
            val cal = Calendar.getInstance()
            val date = ISO_DATE_FORMAT.parse(isoDate) ?: Date()
            cal.time = date
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            ISO_DATE_FORMAT.format(cal.time)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun getEndOfWeek(isoDate: String = todayString()): String {
        return addDays(getStartOfWeek(isoDate), 6)
    }

    fun isDateBefore(date1: String, date2: String): Boolean {
        return date1 < date2
    }

    fun isDateToday(date: String): Boolean {
        return date == todayString()
    }

    fun formatSeconds(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    fun formatMinutesToHours(minutes: Int): String {
        if (minutes < 60) return "${minutes}m"
        val h = minutes / 60
        val m = minutes % 60
        return if (m == 0) "${h}h" else "${h}h ${m}m"
    }

    fun formatSecondsToHours(seconds: Int): String {
        val minutes = seconds / 60
        return formatMinutesToHours(minutes)
    }

    fun getDayStartMillis(isoDate: String): Long {
        return try {
            val cal = Calendar.getInstance()
            val date = ISO_DATE_FORMAT.parse(isoDate) ?: Date()
            cal.time = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        } catch (e: Exception) {
            0L
        }
    }

    fun getDayEndMillis(isoDate: String): Long {
        return getDayStartMillis(isoDate) + 86400000L - 1
    }

    fun dateFromMillis(millis: Long): String {
        return ISO_DATE_FORMAT.format(Date(millis))
    }
}
