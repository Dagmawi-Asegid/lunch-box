# Lunch Box

Android app for finding, sorting, reviewing, and (as a labeled demo) ordering
from restaurants. Real nearby restaurants come from OpenStreetMap's free
Overpass API and appear on an embedded interactive map alongside a curated
list — no billing, no API key, no signup required. Users sign in with
Firebase Auth, browse a restaurant list backed by Cloud Firestore, get
directions, and submit star ratings + written reviews that update each
restaurant's aggregate rating in real time.

Built, installed, and driven end-to-end on a real Android emulator (API 34,
arm64) — first against the [Firebase Local Emulator Suite](https://firebase.google.com/docs/emulator-suite)
for local iteration, then against a real Firebase project (Auth + Firestore)
for a standalone build that works on an actual device. Grab the installable
APK from [Releases](https://github.com/Dagmawi-Asegid/lunch-box/releases).
Not just written and hoped to compile — that process caught and fixed real
bugs along the way (see below).

## Features

- Email/password auth (Firebase Auth) — login and registration screens
- **Real nearby restaurants** via OpenStreetMap's Overpass API, based on
  your actual device location — merged into the same Firestore-backed list
  as the curated demo restaurants, so search/sort/review all work uniformly
  across both
- **Interactive embedded map** (osmdroid, OpenStreetMap tiles) showing your
  location and every nearby restaurant as a tappable pin; tapping a pin
  scrolls the list to that restaurant
- Restaurant list with:
  - Live, case-insensitive search by name
  - Sort by rating, distance, name, or location
  - Pull-to-refresh to re-sync nearby restaurants
  - Distance badge and a cuisine-based icon banner on every card (real photo
    used instead, on the rare OSM entry that has one)
- **Directions** — one tap opens Google Maps with turn-by-turn directions to
  the restaurant (deep link, no Maps API key needed)
- **Order (demo)** — a real cart/checkout flow (menu, quantities, running
  total, notes) that saves a genuine order to your account in Firestore.
  Clearly labeled as a demo in the UI: no real payment is processed and
  nothing is sent to the restaurant, since neither exists behind this app.
- Add a review (star rating + comment) for any restaurant; the restaurant's
  average rating and review count are recomputed and persisted on submit

## Stack

Kotlin, Android SDK (View system + ViewBinding, no Compose), Firebase
Authentication, Cloud Firestore, Google Play Services Location, osmdroid
(OpenStreetMap maps), OkHttp (Overpass API), Glide (image loading), Kotlin
coroutines, MVVM (`ViewModel` + `LiveData` + repository layer).

## Project structure

```
app/src/main/java/com/dagmawiasegid/lunchbox/
  LoginActivity.kt / RegisterActivity.kt
  MainActivity.kt               — restaurant list, map, search, sort, nearby sync
  AddReviewActivity.kt          — submit a review for a restaurant
  OrderActivity.kt              — demo cart/checkout flow
  LunchBoxApp.kt                 — Firebase emulator wiring (opt-in) + osmdroid config
  data/Restaurant.kt, Review.kt, Order.kt, DemoMenu.kt
  data/RestaurantRepository.kt  — Firestore queries + aggregate rating updates
  data/OverpassRepository.kt    — free OpenStreetMap nearby-restaurant search
  data/OrderRepository.kt
  ui/RestaurantViewModel.kt
  ui/adapter/RestaurantAdapter.kt
  util/DistanceUtil.kt          — haversine distance + mile formatting
  util/CuisineIcons.kt          — OSM cuisine tag → emoji mapping
scripts/seed.js                 — seeds sample restaurants into the Firestore emulator
firebase.json / firestore.rules / firestore.indexes.json
```

## Why OpenStreetMap instead of Google Places

Google Places API gives richer data (real ratings, photos, hours) but
requires linking a credit card to a Google Cloud project before it'll answer
a single request — even within the free tier. OpenStreetMap's Overpass API
needs none of that: it's free, keyless, and has real crowd-sourced restaurant
data with decent coverage in most populated areas. Trade-offs: no ratings
(this app shows only its own users' reviews for OSM-sourced restaurants,
honestly starting at "No reviews yet"), photos are rare, and the free public
Overpass instance can occasionally return "server busy" under load — this app
retries once automatically and falls back to the saved restaurant list with a
clear message if that still fails.

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
4. **Overpass API returned HTTP 406.** OkHttp's default User-Agent got
   rejected by the Overpass server. Overpass's own usage policy asks clients
   to identify themselves anyway — fixed by sending a descriptive
   `User-Agent` header.
5. **Firestore rules deployed out of sync with code.** Added a rule for the
   new `orders` collection locally but forgot to redeploy it — real order
   submissions failed with `PERMISSION_DENIED` until `firebase deploy
   --only firestore:rules` was actually run against the live project.

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

Nearby-restaurant search and the embedded map need real device/emulator
location. Android emulators' mock-GPS injection (`adb emu geo fix`) isn't
always picked up reliably by `FusedLocationProviderClient` — this is a known
emulator-tooling quirk, not an app bug; it's been verified working end-to-end
against a real Overpass search and a real device location on this same code
path.

## Installing the release APK

Releases from v1.2.0 onward are built with `./gradlew assembleRelease`,
signed with a dedicated key (`app/release-key.jks`, committed — it's a
self-signed demo cert with no real distribution behind it, not a secret)
instead of Android's shared debug key. Debug-signed APKs are a strong
signal Google Play Protect flags as "unknown," so this cuts down on that
warning — though **any APK installed outside the Play Store will still
show a Play Protect prompt on install**; that's inherent to sideloading,
not something signing alone removes. If you see it, choose "More details"
→ "Install anyway." This isn't malware — it's a fresh, unpublished student
project, which is exactly the profile Play Protect is cautious about by
design.
