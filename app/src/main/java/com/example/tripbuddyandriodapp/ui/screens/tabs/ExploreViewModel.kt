package com.example.tripbuddyandriodapp.ui.screens.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripbuddyandriodapp.data.remote.*
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
        val geocoding: GeocodingResult?
    ) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val weatherApi: WeatherApi,
    private val placesApi: PlacesApi
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
                if (coords != null) {
                    val weather = weatherApi.getWeatherForecast(
                        coords.latitude,
                        coords.longitude,
                        startDate,
                        endDate
                    )
                    val attractions = placesApi.getNearbyPlaces(coords.latitude, coords.longitude)
                    _uiState.value = ExploreUiState.Success(weather, attractions, coords)
                } else {
                    _uiState.value = ExploreUiState.Error("Could not find coordinates for $destination")
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
