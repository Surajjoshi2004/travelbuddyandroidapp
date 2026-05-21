package com.example.tripbuddyandriodapp.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PlaceGeoname(
    val lat: Double,
    val lon: Double
)

@Serializable
data class PlaceRadiusResponse(
    val features: List<PlaceFeature>
)

@Serializable
data class PlaceFeature(
    val properties: PlaceProperties
)

@Serializable
data class PlaceProperties(
    val xid: String,
    val name: String,
    val kinds: String,
    val dist: Double? = null
)

@Serializable
data class PlaceDetail(
    val xid: String,
    val name: String,
    val wikipedia_extracts: WikipediaExtract? = null,
    val preview: PreviewImage? = null,
    val point: PlaceGeoname
)

@Serializable
data class WikipediaExtract(
    val text: String
)

@Serializable
data class PreviewImage(
    val source: String
)

@Singleton
class PlacesApi @Inject constructor(private val client: HttpClient) {
    private val apiKey = "5ae2e3f221c38a28845f05b6255c718e658ba9be8b2ceee367de2dba" // In a real app, this should come from BuildConfig

    suspend fun getGeoname(name: String): PlaceGeoname? {
        return try {
            client.get("https://api.opentripmap.com/0.1/en/places/geoname") {
                parameter("name", name)
                parameter("apikey", apiKey)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getNearbyPlaces(lat: Double, lon: Double): List<PlaceProperties> {
        return try {
            val response: PlaceRadiusResponse = client.get("https://api.opentripmap.com/0.1/en/places/radius") {
                parameter("radius", 10000)
                parameter("lon", lon)
                parameter("lat", lat)
                parameter("kinds", "interesting_places")
                parameter("limit", 10)
                parameter("apikey", apiKey)
            }.body()
            response.features.map { it.properties }.filter { it.name.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPlaceDetail(xid: String): PlaceDetail? {
        return try {
            client.get("https://api.opentripmap.com/0.1/en/places/xid/$xid") {
                parameter("apikey", apiKey)
            }.body()
        } catch (e: Exception) {
            null
        }
    }
}
