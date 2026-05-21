package com.example.tripbuddyandriodapp.data.sync

import com.example.tripbuddyandriodapp.data.local.*
import com.example.tripbuddyandriodapp.data.remote.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val tripDao: TripDao,
    private val dayDao: DayDao,
    private val activityDao: ActivityDao,
    private val packingDao: PackingDao,
    private val expenseDao: ExpenseDao,
    private val supabaseClient: SupabaseClient
) {
    suspend fun sync() = withContext(Dispatchers.IO) {
        try {
            syncTrips()
            syncDays()
            syncActivities()
            syncPackingItems()
            syncExpenses()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncTrips() {
        val unsyncedTrips = tripDao.getUnsyncedTrips()
        unsyncedTrips.forEach { trip ->
            try {
                // In a real app, you'd map this to a DTO and push to Supabase
                // val response = supabaseClient.post("trips", trip.toDto())
                // if (response.status.isSuccess()) {
                tripDao.updateTrip(trip.copy(isSynced = true))
                // }
            } catch (e: Exception) {
                // Handle individual trip sync failure
            }
        }
    }

    private suspend fun syncDays() {
        val unsyncedDays = dayDao.getUnsyncedDays()
        unsyncedDays.forEach { day ->
            dayDao.insertDays(listOf(day.copy(isSynced = true)))
        }
    }

    private suspend fun syncActivities() {
        val unsyncedActivities = activityDao.getUnsyncedActivities()
        unsyncedActivities.forEach { activity ->
            activityDao.updateActivity(activity.copy(isSynced = true))
        }
    }

    private suspend fun syncPackingItems() {
        val unsyncedItems = packingDao.getUnsyncedPackingItems()
        unsyncedItems.forEach { item ->
            packingDao.updatePackingItem(item.copy(isSynced = true))
        }
    }

    private suspend fun syncExpenses() {
        val unsyncedExpenses = expenseDao.getUnsyncedExpenses()
        unsyncedExpenses.forEach { expense ->
            expenseDao.updateExpense(expense.copy(isSynced = true))
        }
    }
}
