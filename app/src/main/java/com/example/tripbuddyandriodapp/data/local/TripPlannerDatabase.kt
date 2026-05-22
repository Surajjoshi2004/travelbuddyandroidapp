package com.example.tripbuddyandriodapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TripEntity::class,
        DayEntity::class,
        ActivityEntity::class,
        PackingItemEntity::class,
        ExpenseEntity::class
    ],
    version = 2, // Bumped version to 2 to trigger unique constraint migration
    exportSchema = false
)
abstract class TripPlannerDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun dayDao(): DayDao
    abstract fun activityDao(): ActivityDao
    abstract fun packingDao(): PackingDao
    abstract fun expenseDao(): ExpenseDao
}
