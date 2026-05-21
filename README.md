# Trip Buddy - Production-Ready Trip Planner

Trip Buddy is a modern, offline-first Android application built with Kotlin and Jetpack Compose. It follows Clean Architecture and MVVM patterns to provide a seamless travel planning experience.

## 🚀 Features
- **Smart Auth**: Quick login/signup to keep your trips synced.
- **Trip Dashboard**: View upcoming, ongoing, and completed trips with live countdowns.
- **Automated Itinerary**: Select dates and a template (Beach, Trekking, etc.) to auto-generate daily activities and packing lists.
- **Packing Tracker**: Category-based packing list with a real-time progress bar.
- **Budget Manager**: Track expenses by category and view total spending.
- **Explore Tab**: Live 7-day weather forecasts, nearby attractions with Google Maps integration, and local food suggestions.

## 🛠 Tech Stack
- **UI**: Jetpack Compose + Material 3 (Dynamic Color support)
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room (Local) & Supabase (Remote Cloud)
- **Networking**: Ktor HTTP Client
- **DI**: Hilt
- **Async**: Kotlin Coroutines & StateFlow

## ⚙️ Setup Instructions

### 1. Database (Supabase)
1. Create a project at [supabase.com](https://supabase.com).
2. Go to the **SQL Editor** and run the following command to create your tables:
```sql
create table trips (id bigint primary key generated always as identity, destination text, start_date bigint, end_date bigint, travelers int, template_used text);
create table days (id bigint primary key generated always as identity, trip_id bigint references trips(id) on delete cascade, day_number int, date bigint);
create table activities (id bigint primary key generated always as identity, day_id bigint references days(id) on delete cascade, time text, title text, description text);
create table packing_items (id bigint primary key generated always as identity, trip_id bigint references trips(id) on delete cascade, name text, is_checked boolean default false, category text);
create table expenses (id bigint primary key generated always as identity, trip_id bigint references trips(id) on delete cascade, category text, amount float, date bigint);
```

### 2. API Keys
1. **Supabase**: Open `data/remote/SupabaseClient.kt` and paste your **Project URL** and **Publishable (anon) key**.
2. **Attractions**: Get a free key from [opentripmap.io](https://opentripmap.io/) and paste it into `data/remote/PlacesApi.kt`.

### 3. Build & Run
1. Open the project in **Android Studio (Ladybug or newer)**.
2. Ensure you are using **JDK 17+**.
3. Sync Gradle and click the **Run** button.

## 📁 Project Structure
- `data/local`: Room Database, DAOs, and Entities.
- `data/remote`: Ktor API clients for Weather, Places, and Supabase.
- `ui/screens`: Individual screens (Home, Auth, Detail, Settings).
- `ui/components`: Reusable UI elements (TripCard, ActivityRow, etc.).
- `utils`: Date math, status logic, and template providers.
