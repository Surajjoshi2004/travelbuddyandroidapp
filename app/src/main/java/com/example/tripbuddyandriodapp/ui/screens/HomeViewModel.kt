package com.example.tripbuddyandriodapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripbuddyandriodapp.data.local.DayEntity
import com.example.tripbuddyandriodapp.data.local.TripEntity
import com.example.tripbuddyandriodapp.data.repository.TripRepository
import com.example.tripbuddyandriodapp.utils.DateUtils
import com.example.tripbuddyandriodapp.utils.TemplateProvider
import com.example.tripbuddyandriodapp.utils.TripStatus
import com.example.tripbuddyandriodapp.utils.TripStatusUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val ongoingTrip: TripEntity? = null,
    val upcomingTrips: List<TripEntity> = emptyList(),
    val totalTrips: Int = 0,
    val citiesExplored: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TripRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.getAllTrips()
        .map { trips ->
            val ongoing = trips.find { TripStatusUtils.getTripStatus(it.startDate, it.endDate) == TripStatus.ONGOING }
            val upcoming = trips.filter { TripStatusUtils.getTripStatus(it.startDate, it.endDate) == TripStatus.UPCOMING }
            
            HomeUiState(
                ongoingTrip = ongoing,
                upcomingTrips = upcoming,
                totalTrips = trips.size,
                citiesExplored = trips.distinctBy { it.destination }.size
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun addTrip(destination: String, startDate: Long, endDate: Long, travelers: Int, template: String) {
        viewModelScope.launch {
            val tripId = repository.insertTrip(
                TripEntity(
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    travelers = travelers,
                    templateUsed = template
                )
            )

            val duration = DateUtils.getDurationInDays(startDate, endDate)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startDate

            val days = (1..duration).map { i ->
                val day = DayEntity(tripId = tripId, dayNumber = i, date = calendar.timeInMillis)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                day
            }
            val dayIds = repository.insertDays(days)

            dayIds.forEach { dayId ->
                repository.insertActivities(TemplateProvider.getDefaultActivities(dayId))
            }
            repository.insertPackingItems(TemplateProvider.getPackingItems(tripId, template))
        }
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }
}
