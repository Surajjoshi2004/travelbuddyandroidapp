package com.example.tripbuddyandriodapp.ui.screens.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripbuddyandriodapp.data.remote.*
import com.example.tripbuddyandriodapp.data.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExploreUiState {
    object Idle : ExploreUiState()
    object Loading : ExploreUiState()
    data class Success(
        val weather: WeatherResponse?,
        val attractions: List<PlaceProperties>,
        val geocoding: GeocodingResult?,
        val aiFoods: List<String> = emptyList(),
        val aiTips: List<String> = emptyList(),
        val aiActivities: List<Pair<String, String>> = emptyList()
    ) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val weatherApi: WeatherApi,
    private val placesApi: PlacesApi,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Idle)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredUiState: StateFlow<ExploreUiState> = combine(_uiState, _searchQuery) { state, query ->
        if (state is ExploreUiState.Success && query.isNotBlank()) {
            state.copy(
                attractions = state.attractions.filter {
                    it.name.contains(query, ignoreCase = true) || 
                    it.kinds.contains(query, ignoreCase = true)
                }
            )
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExploreUiState.Idle)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadExploreData(destination: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            try {
                val coords = weatherApi.getCoordinates(destination)
                val aiFoodData = aiRepository.getLocalFoodAndTips(destination)
                val aiActivityData = aiRepository.getSuggestedActivities(destination)
                
                if (coords != null) {
                    val weather = weatherApi.getWeatherForecast(
                        coords.latitude,
                        coords.longitude,
                        startDate,
                        endDate
                    )
                    val attractions = placesApi.getNearbyPlaces(coords.latitude, coords.longitude)
                    _uiState.value = ExploreUiState.Success(
                        weather = weather, 
                        attractions = attractions, 
                        geocoding = coords,
                        aiFoods = aiFoodData.first,
                        aiTips = aiFoodData.second,
                        aiActivities = aiActivityData
                    )
                } else {
                    _uiState.value = ExploreUiState.Success(
                        weather = null,
                        attractions = emptyList(),
                        geocoding = null,
                        aiFoods = aiFoodData.first,
                        aiTips = aiFoodData.second,
                        aiActivities = aiActivityData
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    suspend fun getPlaceDetail(xid: String): PlaceDetail? {
        return placesApi.getPlaceDetail(xid)
    }
}
