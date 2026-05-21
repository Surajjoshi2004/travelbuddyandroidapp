package com.example.tripbuddyandriodapp.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CurrencyResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

@Singleton
class CurrencyApi @Inject constructor(private val client: HttpClient) {
    suspend fun getExchangeRates(from: String, to: String): CurrencyResponse? {
        return try {
            client.get("https://api.frankfurter.app/latest") {
                parameter("from", from)
                parameter("to", to)
            }.body()
        } catch (e: Exception) {
            null
        }
    }
}
