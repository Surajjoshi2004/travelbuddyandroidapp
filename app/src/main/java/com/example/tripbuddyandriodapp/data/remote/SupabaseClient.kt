package com.example.tripbuddyandriodapp.data.remote

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor(
    private val client: HttpClient
) {
    private val supabaseUrl = "https://tlssuhnrzolpumewbsbf.supabase.co"
    // IMPORTANT: Ensure this is the FULL key from your Supabase dashboard.
    private val supabaseKey = "sb_publishable_ohCfVVVZG6EdLWEWKpgW2w_fEdHnTMG"

    suspend fun post(table: String, body: Any): HttpResponse {
        return client.post("$supabaseUrl/rest/v1/$table") {
            header("apikey", supabaseKey)
            header("Authorization", "Bearer $supabaseKey")
            header("Content-Type", "application/json")
            header("Prefer", "return=representation")
            setBody(body)
        }
    }

    suspend fun get(table: String, query: String = ""): HttpResponse {
        return client.get("$supabaseUrl/rest/v1/$table?$query") {
            header("apikey", supabaseKey)
            header("Authorization", "Bearer $supabaseKey")
        }
    }
}
