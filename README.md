# Lunch Box

Android app for searching, sorting, and reviewing restaurants. Users sign in
with Firebase Auth, browse a restaurant list backed by Cloud Firestore, and
submit star ratings + written reviews that update each restaurant's aggregate
rating in real time.

## Features

- Email/password auth (Firebase Auth) — login and registration screens
- Restaurant list from Cloud Firestore, with:
  - Live search by name
  - Sort by rating, name, or location
- Add a review (star rating + comment) for any restaurant; the restaurant's
  average rating and review count are recomputed and persisted on submit

## Stack

Kotlin, Android SDK (View system + ViewBinding, no Compose), Firebase
Authentication, Cloud Firestore, Kotlin coroutines, MVVM (`ViewModel` +
`LiveData` + repository layer).

## Project structure

```
app/src/main/java/com/dagmawiasegid/lunchbox/
  LoginActivity.kt / RegisterActivity.kt
  MainActivity.kt              — restaurant list, search, sort
  AddReviewActivity.kt         — submit a review for a restaurant
  data/Restaurant.kt, Review.kt
  data/RestaurantRepository.kt — Firestore queries + aggregate rating updates
  ui/RestaurantViewModel.kt
  ui/adapter/RestaurantAdapter.kt
```

## Running this project

This is a standard Gradle-based Android Studio project. To run it you'll need
Android Studio (or the Android SDK + emulator/device) — this repo was written
and tested for correctness by hand, but **not compiled or run on-device**,
since no Android SDK/emulator was available in the environment it was built
in. To build it yourself:

1. Open the project root in Android Studio.
2. Create a Firebase project, add an Android app with package name
   `com.dagmawiasegid.lunchbox`, enable **Authentication (Email/Password)**
   and **Cloud Firestore**, and download `google-services.json` into `app/`.
3. Seed a `restaurants` collection in Firestore with documents shaped like
   `Restaurant` in `data/Restaurant.kt` (`name`, `location`, `cuisine`,
   `averageRating`, `reviewCount`).
4. Run on an emulator or device.
