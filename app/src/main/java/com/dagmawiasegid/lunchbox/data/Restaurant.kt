package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.DocumentId

data class Restaurant(
    @DocumentId val id: String = "",
    val name: String = "",
    val location: String = "",
    val cuisine: String = "",
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    // Populated for restaurants imported from OpenStreetMap nearby search
    // (via the free Overpass API — no billing/API key required).
    val osmId: String? = null,
    val amenityType: String? = null,
    val brand: String? = null,
    val photoUrl: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
