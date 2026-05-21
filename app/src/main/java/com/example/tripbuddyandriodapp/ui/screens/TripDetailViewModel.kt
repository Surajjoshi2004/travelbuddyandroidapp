package com.example.tripbuddyandriodapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripbuddyandriodapp.data.local.*
import com.example.tripbuddyandriodapp.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repository: TripRepository
) : ViewModel() {

    private val _tripId = MutableStateFlow<Long?>(null)
    
    val trip: StateFlow<TripEntity?> = _tripId
        .filterNotNull()
        .flatMapLatest { id -> 
            flow { emit(repository.getTripById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Improved days flow to handle auto-generation reliably
    val days: StateFlow<List<DayEntity>> = _tripId
        .filterNotNull()
        .flatMapLatest { id -> repository.getDaysForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trigger day generation when trip data is available if no days exist
    init {
        viewModelScope.launch {
            combine(_tripId.filterNotNull(), trip.filterNotNull(), days) { id, tripObj, daysList ->
                if (daysList.isEmpty()) {
                    generateFirstDay(id, tripObj.startDate)
                }
            }.collect()
        }
    }

    val packingItems: StateFlow<List<PackingItemEntity>> = _tripId
        .filterNotNull()
        .flatMapLatest { id -> repository.getPackingItemsForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = _tripId
        .filterNotNull()
        .flatMapLatest { id -> repository.getExpensesForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTripId(id: Long) {
        _tripId.value = id
    }

    private suspend fun generateFirstDay(tripId: Long, startDate: Long) {
        repository.insertDays(listOf(DayEntity(tripId = tripId, dayNumber = 1, date = startDate)))
    }

    fun togglePackingItem(item: PackingItemEntity) {
        viewModelScope.launch {
            repository.updatePackingItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun addActivity(dayId: Long, time: String, title: String, description: String) {
        viewModelScope.launch {
            repository.insertActivity(
                ActivityEntity(dayId = dayId, time = time, title = title, description = description)
            )
        }
    }

    fun addExpense(category: String, amount: Double, date: Long) {
        val tripId = _tripId.value ?: return
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(tripId = tripId, category = category, amount = amount, date = date)
            )
        }
    }

    fun getActivitiesForDay(dayId: Long): Flow<List<ActivityEntity>> {
        return repository.getActivitiesForDay(dayId)
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}
