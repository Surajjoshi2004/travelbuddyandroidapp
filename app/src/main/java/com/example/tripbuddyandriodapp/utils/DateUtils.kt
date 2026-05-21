package com.example.tripbuddyandriodapp.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dayMonthFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))

    fun formatDayMonth(timestamp: Long): String = dayMonthFormatter.format(Date(timestamp))

    fun formatIso(timestamp: Long): String = isoFormatter.format(Date(timestamp))

    fun formatTripDates(start: Long, end: Long): String {
        return "${formatDate(start)} → ${formatDate(end)}"
    }

    fun getDurationInDays(start: Long, end: Long): Int {
        val diff = end - start
        return (TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1).coerceAtLeast(1)
    }

    fun getDaysRemaining(startDate: Long): Long {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val start = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return TimeUnit.MILLISECONDS.toDays(start - today)
    }
}
