package com.example.tripbuddyandriodapp.data.repository

import com.example.tripbuddyandriodapp.data.local.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val dayDao: DayDao,
    private val activityDao: ActivityDao,
    private val packingDao: PackingDao,
    private val expenseDao: ExpenseDao
) {
    // Trips
    fun getAllTrips(): Flow<List<TripEntity>> = tripDao.getAllTrips()
    suspend fun getTripById(id: Long) = tripDao.getTripById(id)
    suspend fun insertTrip(trip: TripEntity): Long = tripDao.insertTrip(trip)
    suspend fun updateTrip(trip: TripEntity) = tripDao.updateTrip(trip)
    suspend fun deleteTrip(trip: TripEntity) = tripDao.deleteTrip(trip)

    // Days
    fun getDaysForTrip(tripId: Long): Flow<List<DayEntity>> = dayDao.getDaysForTrip(tripId)
    suspend fun insertDays(days: List<DayEntity>): List<Long> = dayDao.insertDays(days)

    // Activities
    fun getActivitiesForDay(dayId: Long): Flow<List<ActivityEntity>> = activityDao.getActivitiesForDay(dayId)
    suspend fun insertActivity(activity: ActivityEntity) = activityDao.insertActivity(activity)
    suspend fun insertActivities(activities: List<ActivityEntity>) = activityDao.insertActivities(activities)
    suspend fun updateActivity(activity: ActivityEntity) = activityDao.updateActivity(activity)
    suspend fun deleteActivity(activity: ActivityEntity) = activityDao.deleteActivity(activity)

    // Packing Items
    fun getPackingItemsForTrip(tripId: Long): Flow<List<PackingItemEntity>> = packingDao.getPackingItemsForTrip(tripId)
    suspend fun insertPackingItems(items: List<PackingItemEntity>) = packingDao.insertPackingItems(items)
    suspend fun updatePackingItem(item: PackingItemEntity) = packingDao.updatePackingItem(item)
    suspend fun deletePackingItem(item: PackingItemEntity) = packingDao.deletePackingItem(item)

    // Expenses
    fun getExpensesForTrip(tripId: Long): Flow<List<ExpenseEntity>> = expenseDao.getExpensesForTrip(tripId)
    suspend fun insertExpense(expense: ExpenseEntity) = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)
}
