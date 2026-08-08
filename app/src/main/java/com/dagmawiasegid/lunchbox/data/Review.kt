package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    val id: String = "",
    val restaurantId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
