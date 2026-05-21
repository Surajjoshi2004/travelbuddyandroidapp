package com.example.tripbuddyandriodapp.utils

import com.example.tripbuddyandriodapp.data.local.ActivityEntity
import com.example.tripbuddyandriodapp.data.local.ExpenseEntity
import com.example.tripbuddyandriodapp.data.local.PackingItemEntity

object TemplateProvider {
    fun getPackingItems(tripId: Long, template: String): List<PackingItemEntity> {
        val items = when (template) {
            "Beach" -> listOf(
                "Swimsuit" to "Clothing", "Sunscreen" to "Toiletries", "Sunglasses" to "Accessories",
                "Beach Towel" to "Other", "Flip Flops" to "Clothing", "Hat" to "Accessories"
            )
            "Business" -> listOf(
                "Formal Shirt x3" to "Clothing", "Blazer" to "Clothing", "Laptop" to "Electronics",
                "Business Cards" to "Other", "Formal Shoes" to "Clothing", "Notebook & Pen" to "Other"
            )
            "Trekking" -> listOf(
                "Trekking Boots" to "Clothing", "Rain Jacket" to "Clothing", "First Aid Kit" to "Other",
                "Flashlight" to "Electronics", "Compass" to "Other", "Energy Bars" to "Food"
            )
            else -> listOf(
                "T-shirts x3" to "Clothing", "Jeans x2" to "Clothing", "Toothbrush" to "Toiletries",
                "Phone Charger" to "Electronics", "Wallet & ID" to "Essentials", "Medicines" to "Other"
            )
        }
        return items.map { PackingItemEntity(tripId = tripId, name = it.first, category = it.second) }
    }

    fun getDefaultActivities(dayId: Long): List<ActivityEntity> {
        return listOf(
            ActivityEntity(dayId = dayId, time = "09:00", title = "Breakfast & Briefing", description = "Plan the day over local coffee"),
            ActivityEntity(dayId = dayId, time = "11:00", title = "Explore Top Attraction", description = "Visit the most famous spot nearby"),
            ActivityEntity(dayId = dayId, time = "14:00", title = "Local Food Experience", description = "Try a highly-rated local dish"),
            ActivityEntity(dayId = dayId, time = "19:00", title = "Relaxation & Dinner", description = "Wind down and enjoy the evening")
        )
    }
}
