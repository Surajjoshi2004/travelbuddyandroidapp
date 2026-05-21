package com.example.tripbuddyandriodapp.di

import android.content.Context
import androidx.room.Room
import com.example.tripbuddyandriodapp.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TripPlannerDatabase {
        return Room.databaseBuilder(
            context,
            TripPlannerDatabase::class.java,
            "trip_planner_db"
        )
        .fallbackToDestructiveMigration() // Add this to prevent crashes on schema changes
        .build()
    }

    @Provides
    fun provideTripDao(db: TripPlannerDatabase): TripDao = db.tripDao()

    @Provides
    fun provideDayDao(db: TripPlannerDatabase): DayDao = db.dayDao()

    @Provides
    fun provideActivityDao(db: TripPlannerDatabase): ActivityDao = db.activityDao()

    @Provides
    fun providePackingDao(db: TripPlannerDatabase): PackingDao = db.packingDao()

    @Provides
    fun provideExpenseDao(db: TripPlannerDatabase): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }
    }
}
