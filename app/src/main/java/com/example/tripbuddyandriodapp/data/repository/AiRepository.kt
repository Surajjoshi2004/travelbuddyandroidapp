package com.example.tripbuddyandriodapp.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    suspend fun getSuggestedActivities(destination: String): List<Pair<String, String>> {
        return try {
            withTimeout(10000) {
                val prompt = "Suggest 5 popular tourist activities for $destination. Format strictly as 'Title: Description'."
                val response = generativeModel.generateContent(content { text(prompt) })
                val suggestions = parseAiResponse(response.text)
                if (suggestions.isEmpty()) getFallbackActivities(destination) else suggestions
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "AI Activity Fetch Failed: ${e.message}")
            getFallbackActivities(destination)
        }
    }

    suspend fun getSuggestedPackingItems(destination: String): List<Pair<String, String>> {
        return try {
            withTimeout(10000) {
                val prompt = "Suggest 8 essential packing items for $destination. Format: Item: Category"
                val response = generativeModel.generateContent(content { text(prompt) })
                val suggestions = parseAiResponse(response.text)
                if (suggestions.isEmpty()) getFallbackPacking(destination) else suggestions
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "AI Packing Fetch Failed: ${e.message}")
            getFallbackPacking(destination)
        }
    }

    fun getFallbackActivities(destination: String): List<Pair<String, String>> {
        return listOf(
            "Sightseeing" to "Explore local landmarks and city highlights in $destination",
            "Local Food Tour" to "Discover the best local eateries and dishes",
            "Museum Visit" to "Learn about the history and culture",
            "City Walk" to "Discover hidden gems on a walking tour",
            "Relaxation" to "Enjoy a peaceful afternoon at a local park"
        )
    }

    fun getFallbackPacking(destination: String): List<Pair<String, String>> {
        return listOf(
            "Passport & ID" to "Essentials", "Universal Adapter" to "Electronics",
            "Power Bank" to "Electronics", "Walking Shoes" to "Footwear",
            "Rain Jacket" to "Clothing", "Personal Medicines" to "Health"
        )
    }

    suspend fun getItineraryForDay(destination: String, dayNumber: Int): List<Triple<String, String, String>> {
        return try {
            withTimeout(15000) {
                val prompt = "Plan Day $dayNumber in $destination. 4 activities. Format: Time | Title | Description"
                val response = generativeModel.generateContent(content { text(prompt) })
                val items = response.text?.lines()?.filter { it.contains("|") }?.map { line ->
                    val parts = line.split("|").map { it.trim() }
                    Triple(parts.getOrElse(0) { "09:00 AM" }, parts.getOrElse(1) { "Activity" }, parts.getOrElse(2) { "Description" })
                } ?: emptyList()
                if (items.isEmpty()) getFallbackItinerary(destination) else items
            }
        } catch (e: Exception) {
            getFallbackItinerary(destination)
        }
    }

    private fun getFallbackItinerary(destination: String): List<Triple<String, String, String>> {
        return listOf(
            Triple("09:00 AM", "Breakfast", "Start your day with local delicacies"),
            Triple("11:00 AM", "Main Attraction", "Visit the most famous spot in $destination"),
            Triple("02:00 PM", "Lunch & Walk", "Try local street food and explore the area"),
            Triple("07:00 PM", "Dinner", "Enjoy a relaxing evening meal")
        )
    }

    private fun parseAiResponse(text: String?): List<Pair<String, String>> {
        if (text.isNullOrBlank()) return emptyList()
        return text.lines().mapNotNull { line ->
            // Handle various formats: "- Title: Desc", "1. Title: Desc", "**Title**: Desc", etc.
            val cleanLine = line.trim()
                .removePrefix("- ")
                .removePrefix("* ")
                .replace(Regex("^\\d+\\.\\s*"), "") // Remove "1. "
            
            val separator = when {
                cleanLine.contains(":") -> ":"
                cleanLine.contains(" - ") -> " - "
                else -> null
            }

            if (separator != null) {
                val parts = cleanLine.split(separator, limit = 2)
                if (parts.size >= 2) {
                    val title = parts[0].trim().replace("**", "").replace("__", "")
                    val description = parts[1].trim().replace("**", "").replace("__", "")
                    if (title.isNotBlank()) return@mapNotNull title to description
                }
            }
            null
        }
    }

    suspend fun getSurpriseActivity(destination: String): Pair<String, String>? {
        return try {
            val response = generativeModel.generateContent("Unique activity in $destination. Format: Title: Description")
            parseAiResponse(response.text).firstOrNull() ?: getFallbackActivities(destination).random()
        } catch (e: Exception) { getFallbackActivities(destination).random() }
    }

    suspend fun getLocalFoodAndTips(destination: String): Pair<List<String>, List<String>> {
        return try {
            val response = generativeModel.generateContent("3 local dishes and 3 tips for $destination. Format: Food: d1, d2, d3 and Tips: t1, t2, t3")
            val text = response.text ?: ""
            val foods = text.lines().find { it.contains("Food:", true) }?.split(":")?.getOrNull(1)?.split(",")?.map { it.trim() } ?: emptyList()
            val tips = text.lines().find { it.contains("Tips:", true) }?.split(":")?.getOrNull(1)?.split(",")?.map { it.trim() } ?: emptyList()
            foods to tips
        } catch (e: Exception) { emptyList<String>() to emptyList<String>() }
    }
}
