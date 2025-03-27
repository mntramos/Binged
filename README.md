# 📺 Binged

Binged is an Android app built with Jetpack Compose that helps users track TV shows they watch. It allows users to track shows, log episodes, and view show details.

## 📌 Features
- **Diary**: Lists all watched episodes in chronological order.
- **Show List**: Displays a grid of tracked TV shows.
- **Show Details**: View information about a show, including air date, rating, and episodes.
- **Episode Logging**: Keep track of watched episodes.

## 💻 Tech Stack
- **Kotlin** with Jetpack Compose
- **Coil** for image loading
- **Koin** for dependency injection
- **TMDB API** for fetching show data

## 🛠️ Building the App
1. Get your **TMDB API Read Access Token** from [The Movie Database](https://www.themoviedb.org/settings/api).
2. Open your project's `local.properties` file.
3. Add the following line:
   ```properties
   TMDB_KEY=your_tmdb_api_read_access_token
