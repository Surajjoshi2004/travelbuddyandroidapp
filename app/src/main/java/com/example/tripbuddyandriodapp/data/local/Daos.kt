package com.example.tripbuddyandriodapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startDate ASC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE isSynced = 0")
    suspend fun getUnsyncedTrips(): List<TripEntity>
}

@Dao
interface DayDao {

    @Query("SELECT * FROM days WHERE tripId = :tripId ORDER BY dayNumber ASC")
    fun getDaysForTrip(tripId: Long): Flow<List<DayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<DayEntity>): List<Long>

    @Delete
    suspend fun deleteDay(day: DayEntity)

    @Query("SELECT MAX(dayNumber) FROM days WHERE tripId = :tripId")
    suspend fun getLastDayNumber(tripId: Long): Int?

    @Query("SELECT * FROM days WHERE isSynced = 0")
    suspend fun getUnsyncedDays(): List<DayEntity>
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE dayId = :dayId ORDER BY time ASC")
    fun getActivitiesForDay(dayId: Long): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityEntity>)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activities WHERE isSynced = 0")
    suspend fun getUnsyncedActivities(): List<ActivityEntity>
}

@Dao
interface PackingDao {
    @Query("SELECT * FROM packing_items WHERE tripId = :tripId")
    fun getPackingItemsForTrip(tripId: Long): Flow<List<PackingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItems(items: List<PackingItemEntity>)

    @Update
    suspend fun updatePackingItem(item: PackingItemEntity)

    @Delete
    suspend fun deletePackingItem(item: PackingItemEntity)

    @Query("SELECT * FROM packing_items WHERE isSynced = 0")
    suspend fun getUnsyncedPackingItems(): List<PackingItemEntity>
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY date DESC")
    fun getExpensesForTrip(tripId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<ExpenseEntity>
}
