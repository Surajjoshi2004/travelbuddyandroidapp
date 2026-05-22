package com.example.tripbuddyandriodapp.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripbuddyandriodapp.data.local.*
import com.example.tripbuddyandriodapp.data.repository.AiRepository
import com.example.tripbuddyandriodapp.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repository: TripRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _tripId = MutableStateFlow<Long?>(null)
    
    val trip: StateFlow<TripEntity?> = _tripId
        .filterNotNull()
        .flatMapLatest { id -> 
            flow { emit(repository.getTripById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val days: StateFlow<List<DayEntity>> = _tripId
        .filterNotNull()
        .flatMapLatest { id -> repository.getDaysForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiActivitySuggestions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiActivitySuggestions: StateFlow<List<Pair<String, String>>> = _aiActivitySuggestions

    private val _aiPackingSuggestions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiPackingSuggestions: StateFlow<List<Pair<String, String>>> = _aiPackingSuggestions

    private val _isGeneratingItinerary = MutableStateFlow(false)
    val isGeneratingItinerary: StateFlow<Boolean> = _isGeneratingItinerary

    private val _isFetchingSuggestions = MutableStateFlow(false)
    val isFetchingSuggestions: StateFlow<Boolean> = _isFetchingSuggestions

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError

    init {
        // Removed redundant day generation that caused duplicate "Day 1" entries.
        // Days are now correctly handled by HomeViewModel during Trip creation.

        // Only trigger AI suggestions if the destination actually changes
        viewModelScope.launch {
            trip.filterNotNull()
                .map { it.destination }
                .distinctUntilChanged()
                .collectLatest { destination ->
                    if (destination.isNotBlank()) {
                        _aiActivitySuggestions.value = aiRepository.getFallbackActivities(destination)
                        _aiPackingSuggestions.value = aiRepository.getFallbackPacking(destination)
                        loadAiSuggestions(destination)
                    }
                }
        }
    }

    fun refreshAiSuggestions() {
        val dest = trip.value?.destination ?: return
        viewModelScope.launch { loadAiSuggestions(dest) }
    }

    private suspend fun loadAiSuggestions(destination: String) {
        _isFetchingSuggestions.value = true
        _aiError.value = null
        try {
            val activities = aiRepository.getSuggestedActivities(destination)
            val packing = aiRepository.getSuggestedPackingItems(destination)
            
            if (activities.isNotEmpty()) _aiActivitySuggestions.value = activities
            if (packing.isNotEmpty()) _aiPackingSuggestions.value = packing
            
        } catch (e: Exception) {
            _aiError.value = "AI offline. Showing local suggestions."
            Log.e("TripDetailViewModel", "AI Error: ${e.message}")
        } finally {
            _isFetchingSuggestions.value = false
        }
    }

    fun generateAiItineraryForDay(dayId: Long, dayNumber: Int) {
        val destination = trip.value?.destination ?: return
        viewModelScope.launch {
            _isGeneratingItinerary.value = true
            try {
                val activities = aiRepository.getItineraryForDay(destination, dayNumber)
                activities.forEach { (time, title, desc) ->
                    addActivity(dayId, time, title, desc)
                }
            } catch (e: Exception) {
                Log.e("TripDetailViewModel", "Itinerary generation failed", e)
            } finally {
                _isGeneratingItinerary.value = false
            }
        }
    }

    suspend fun getAiSurpriseActivity(): Pair<String, String>? {
        val destination = trip.value?.destination ?: return null
        return aiRepository.getSurpriseActivity(destination)
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

    fun togglePackingItem(item: PackingItemEntity) {
        viewModelScope.launch {
            repository.updatePackingItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun addPackingItem(name: String, category: String) {
        val tripId = _tripId.value ?: return
        viewModelScope.launch {
            repository.insertPackingItems(listOf(PackingItemEntity(tripId = tripId, name = name, category = category)))
        }
    }

    fun deletePackingItem(item: PackingItemEntity) {
        viewModelScope.launch {
            repository.deletePackingItem(item)
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
