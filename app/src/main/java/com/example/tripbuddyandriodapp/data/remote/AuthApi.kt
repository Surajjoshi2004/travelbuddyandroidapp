package com.example.tripbuddyandriodapp.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val access_token: String? = null,
    val user: UserInfo? = null,
    val msg: String? = null,
    val error: String? = null,
    val error_description: String? = null,
    val code: JsonElement? = null
)

@Serializable
data class UserInfo(
    val id: String,
    val email: String? = null
)

@Singleton
class AuthApi @Inject constructor(
    private val client: HttpClient
) {
    private val supabaseUrl = "https://tlssuhnrzolpumewbsbf.supabase.co"
    private val supabaseKey = "sb_publishable_ohCfVVVZG6EdLWEWKpgW2w_fEdHnTMG"

    suspend fun login(email: String, pass: String): Result<AuthResponse> {
        return try {
            val response = client.post("$supabaseUrl/auth/v1/token?grant_type=password") {
                header("apikey", supabaseKey)
                header("Content-Type", "application/json")
                setBody(AuthRequest(email, pass))
            }

            handleResponse(response, "Login")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, pass: String): Result<AuthResponse> {
        return try {
            val response = client.post("$supabaseUrl/auth/v1/signup") {
                header("apikey", supabaseKey)
                header("Content-Type", "application/json")
                setBody(AuthRequest(email, pass))
            }

            handleResponse(response, "Signup")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to handle Supabase responses and specific error codes like 429
     */
    private suspend fun handleResponse(response: HttpResponse, action: String): Result<AuthResponse> {
        return when {
            response.status.isSuccess() -> {
                Result.success(response.body<AuthResponse>())
            }
            response.status.value == 429 -> {
                // This captures the "Email rate exceeded" or "Too many requests" error
                Result.failure(Exception("Too many attempts. Please wait a few minutes before trying again."))
            }
            else -> {
                val errorBody = try { response.body<AuthResponse>() } catch (e: Exception) { null }
                val errorMessage = errorBody?.error_description
                    ?: errorBody?.error
                    ?: errorBody?.msg
                    ?: "$action failed with status ${response.status.value}"
                Result.failure(Exception(errorMessage))
            }
        }
    }
}