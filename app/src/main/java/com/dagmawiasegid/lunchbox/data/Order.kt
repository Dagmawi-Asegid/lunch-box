package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class OrderLineItem(
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)

/**
 * A demo order — saved to Firestore so the cart/checkout flow is backed by
 * real persistence, but this is explicitly NOT connected to any payment
 * processor and no restaurant receives it. See OrderActivity's disclaimer.
 */
data class Order(
    @DocumentId val id: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val userId: String = "",
    val items: List<OrderLineItem> = emptyList(),
    val total: Double = 0.0,
    val notes: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
