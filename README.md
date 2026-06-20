# Binged

Binged is an Android app built with Jetpack Compose that helps users track TV shows they watch.

## Features

- **Auth** — Email/password and Google sign-in with email verification
- **Shows** — Tracked show library in grid/list view with favorites and watching sections
- **Search** — TMDB search with infinite scroll and inline track/untrack
- **Show Details** — Full show info, episode list, mark episodes as watched
- **Episode Logging** — Log watched episodes with date, notes, and TMDB verification
- **Diary** — Chronological diary of all logged episodes grouped by month
- **Settings** — Sign out, export/import diary, delete account

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Navigation | Jetpack Navigation Compose |
| Networking | Retrofit + OkHttp + Gson |
| Local DB | Room |
| Remote Sync | Firebase Firestore |
| Auth | Firebase Authentication |
| Image Loading | Coil |
| Crash Reporting | Firebase Crashlytics |

## Architecture

Clean Architecture with MVVM: `Feature Screens → ViewModel → Use Case → Repository → Room / Retrofit / Firestore`

## Building the App

1. Get your **TMDB API Read Access Token** from [The Movie Database](https://www.themoviedb.org/settings/api).
2. Open `local.properties` and add:
   ```properties
   TMDB_KEY=your_tmdb_api_read_access_token
   ```
3. (Optional) Add `google-services.json` for Firebase features.
4. Build: `./gradlew assembleDebug`

## Requirements

- Android API 29+ (minSdk)
- Kotlin 2.0.0 / AGP 8.8.0 / Gradle 8.10.2
