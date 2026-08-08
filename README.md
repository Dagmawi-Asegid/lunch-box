# Lunch Box

Android app for searching, sorting, and reviewing restaurants. Users sign in
with Firebase Auth, browse a restaurant list backed by Cloud Firestore, and
submit star ratings + written reviews that update each restaurant's aggregate
rating in real time.

Built, installed, and driven end-to-end on a real Android emulator (API 34,
arm64) against the [Firebase Local Emulator Suite](https://firebase.google.com/docs/emulator-suite)
— not just written and hoped to compile. That process caught and fixed three
real bugs along the way (see below).

## Features

- Email/password auth (Firebase Auth) — login and registration screens
- Restaurant list from Cloud Firestore, with:
  - Live, case-insensitive search by name
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
  LunchBoxApp.kt                — wires Firebase SDKs to the local emulators in debug builds
  data/Restaurant.kt, Review.kt
  data/RestaurantRepository.kt — Firestore queries + aggregate rating updates
  ui/RestaurantViewModel.kt
  ui/adapter/RestaurantAdapter.kt
scripts/seed.js                 — seeds sample restaurants into the Firestore emulator
firebase.json / firestore.rules / firestore.indexes.json
```

## Bugs found and fixed by actually running it

1. **Invisible "Create an account" button.** A plain `<Button>` with a
   transparent background inherits Material's default white text color, so
   it was unreadable on the white login screen. Fixed by setting an explicit
   `textColor`.
2. **Double-counted review aggregates.** `submitReview()` added the new
   review, then queried "existing" reviews for the average — but that query
   ran *after* the write, so it already included the just-added review,
   inflating both the count and (in some cases) the average. Fixed by
   fetching existing reviews *before* adding the new one.
3. **Case-sensitive, prefix-only search.** Search used a Firestore range
   query (`>= name <= name + high-value`), which compares bytes — so a
   lowercase query would never match a capitalized restaurant name, and only
   prefixes worked at all. Replaced with client-side, case-insensitive
   substring filtering over the already-loaded list.

Also: `Restaurant.id` and `Review.id` were missing `@DocumentId`, so every
fetched restaurant/review had an empty `id` — reviews couldn't be linked
back to a restaurant. Fixed by annotating both.

## Running this project

This is a standard Gradle-based Android project with a wrapper (`./gradlew`),
so you don't need Android Studio to build it — though you do need the Android
SDK and an emulator or device to run it.

The committed `app/google-services.json` and `firebase.json`/`.firebaserc`
point at a `demo-lunchbox` project, which only works against the **local**
Firebase Emulator Suite (no real Firebase project needed to try this out).

```bash
# 1. Start the Firebase emulators (Auth + Firestore)
firebase emulators:start --project=demo-lunchbox --only auth,firestore

# 2. Seed sample restaurants
cd scripts && npm install && node seed.js

# 3. Build and install on a running emulator/device
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

To run against a real Firebase project instead, replace
`app/google-services.json` with your own (from the Firebase console) and
remove the `useEmulator(...)` calls in `LunchBoxApp.kt`.
