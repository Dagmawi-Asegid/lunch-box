package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

enum class SortOption { NAME, LOCATION, RATING }

class RestaurantRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val restaurants = firestore.collection("restaurants")
    private val reviews = firestore.collection("reviews")

    suspend fun fetchRestaurants(
        sortBy: SortOption,
        locationFilter: String? = null
    ): List<Restaurant> {
        var query: Query = when (sortBy) {
            SortOption.NAME -> restaurants.orderBy("name")
            SortOption.LOCATION -> restaurants.orderBy("location")
            SortOption.RATING -> restaurants.orderBy("averageRating", Query.Direction.DESCENDING)
        }

        if (!locationFilter.isNullOrBlank()) {
            query = query
                .whereGreaterThanOrEqualTo("location", locationFilter)
                .whereLessThanOrEqualTo("location", locationFilter + "")
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
