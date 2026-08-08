package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

enum class SortOption { NAME, LOCATION, RATING, DISTANCE }

class RestaurantRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val restaurants = firestore.collection("restaurants")
    private val reviews = firestore.collection("reviews")

    /**
     * Imports/refreshes a restaurant discovered via OpenStreetMap nearby
     * search. Keyed by a sanitized osmId so repeated syncs update the same
     * document instead of creating duplicates. Deliberately omits
     * averageRating/reviewCount from the merge payload so this never
     * clobbers our own users' review data — those fields are only ever
     * written by [submitReview].
     */
    suspend fun upsertFromPlace(place: NearbyPlace) {
        val docId = place.osmId.replace("/", "_")
        val data = mutableMapOf<String, Any?>(
            "name" to place.name,
            "location" to place.address,
            "cuisine" to place.cuisine,
            "osmId" to place.osmId,
            "address" to place.address,
            "latitude" to place.latitude,
            "longitude" to place.longitude
        )
        if (place.photoUrl != null) data["photoUrl"] = place.photoUrl

        restaurants.document(docId).set(data, SetOptions.merge()).await()
    }

    suspend fun fetchRestaurants(
        sortBy: SortOption,
        locationFilter: String? = null
    ): List<Restaurant> {
        var query: Query = when (sortBy) {
            SortOption.NAME -> restaurants.orderBy("name")
            SortOption.LOCATION -> restaurants.orderBy("location")
            SortOption.RATING -> restaurants.orderBy("averageRating", Query.Direction.DESCENDING)
            // Distance depends on the user's current position, which isn't
            // something Firestore can sort by server-side here — the caller
            // re-sorts the returned list client-side once it knows where
            // the user is.
            SortOption.DISTANCE -> restaurants.orderBy("name")
        }

        if (!locationFilter.isNullOrBlank()) {
            query = query
                .whereGreaterThanOrEqualTo("location", locationFilter)
                .whereLessThanOrEqualTo("location", locationFilter + "")
        }

        return query.get().await().toObjects(Restaurant::class.java)
    }

    suspend fun getReviewsFor(restaurantId: String): List<Review> {
        return reviews
            .whereEqualTo("restaurantId", restaurantId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Review::class.java)
    }

    /**
     * Adds a review, then recomputes and persists the restaurant's aggregate
     * rating/review count so list sorting and filtering stay accurate.
     *
     * Fetches existing reviews *before* adding the new one — querying after
     * the add would double-count the just-written review, since Firestore's
     * read-after-write consistency means it's already visible to the query.
     */
    suspend fun submitReview(restaurantId: String, review: Review) {
        val existing = getReviewsFor(restaurantId)
        reviews.add(review).await()

        val newCount = existing.size + 1
        val newAverage = (existing.sumOf { it.rating.toDouble() } + review.rating) / newCount

        restaurants.document(restaurantId)
            .update(
                mapOf(
                    "averageRating" to newAverage,
                    "reviewCount" to newCount
                )
            )
            .await()
    }
}
