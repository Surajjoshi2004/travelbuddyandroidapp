package com.example.tripbuddyandriodapp.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class GeocodingResponse(val results: List<GeocodingResult>? = null)

@Serializable
data class GeocodingResult(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val country: String? = null
)

@Serializable
data class WeatherResponse(
    val daily: DailyData
)

@Serializable
data class DailyData(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val precipitation_probability_max: List<Int>,
    val weathercode: List<Int>
)

@Singleton
class WeatherApi @Inject constructor(private val client: HttpClient) {
    suspend fun getCoordinates(query: String): GeocodingResult? {
        val response: GeocodingResponse = client.get("https://geocoding-api.open-meteo.com/v1/search") {
            parameter("name", query)
            parameter("count", 1)
        }.body()
        return response.results?.firstOrNull()
    }

    suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        startDate: String,
        endDate: String
    ): WeatherResponse {
        return client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", lat)
            parameter("longitude", lon)
            parameter("daily", "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode")
            parameter("timezone", "auto")
            parameter("start_date", startDate)
            parameter("end_date", endDate)
        }.body()
    }
}
