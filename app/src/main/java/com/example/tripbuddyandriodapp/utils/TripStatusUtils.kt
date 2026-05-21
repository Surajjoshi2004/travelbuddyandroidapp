package com.example.tripbuddyandriodapp.utils

import androidx.compose.ui.graphics.Color
import com.example.tripbuddyandriodapp.ui.theme.StatusCompleted
import com.example.tripbuddyandriodapp.ui.theme.StatusOngoing
import com.example.tripbuddyandriodapp.ui.theme.StatusUpcoming
import java.util.*

enum class TripStatus {
    UPCOMING, ONGOING, COMPLETED
}

object TripStatusUtils {
    fun getTripStatus(startDate: Long, endDate: Long): TripStatus {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return when {
            today < startDate -> TripStatus.UPCOMING
            today > endDate -> TripStatus.COMPLETED
            else -> TripStatus.ONGOING
        }
    }

    fun getStatusColor(status: TripStatus): Color {
        return when (status) {
            TripStatus.UPCOMING -> StatusUpcoming
            TripStatus.ONGOING -> StatusOngoing
            TripStatus.COMPLETED -> StatusCompleted
        }
    }

    fun getStatusText(status: TripStatus, startDate: Long, endDate: Long): String {
        val daysRemaining = DateUtils.getDaysRemaining(startDate)
        return when (status) {
            TripStatus.UPCOMING -> {
                when {
                    daysRemaining > 1 -> "✈️ In $daysRemaining days"
                    daysRemaining == 1L -> "✈️ Tomorrow!"
                    else -> "✈️ Today!"
                }
            }
            TripStatus.ONGOING -> {
                val today = Calendar.getInstance().timeInMillis
                val dayOfTrip = DateUtils.getDurationInDays(startDate, today)
                val totalDays = DateUtils.getDurationInDays(startDate, endDate)
                "🟢 Ongoing – Day $dayOfTrip of $totalDays"
            }
            TripStatus.COMPLETED -> "✅ Completed"
        }
    }
}
