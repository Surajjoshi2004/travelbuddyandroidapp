package com.example.tripbuddyandriodapp.di

import android.content.Context
import androidx.room.Room
import com.example.tripbuddyandriodapp.BuildConfig
import com.example.tripbuddyandriodapp.data.local.*
import com.google.ai.client.generativeai.GenerativeModel
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
        .fallbackToDestructiveMigration()
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

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        // You should provide your API Key in local.properties and access via BuildConfig
        // or a safer method for production apps.
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyBhGO-4KNhoyaklNG4JBvEIU-UHg2EY2Rk" // Replace with BuildConfig.GEMINI_API_KEY
        )
    }
}
