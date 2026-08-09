package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * A promotion/deal shown in the Deals section. These are sample/demo
 * content, not real active discounts — this app has no partnership with
 * any restaurant and can't issue redeemable codes. See DealsActivity's
 * on-screen disclaimer.
 */
data class Promotion(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val restaurantName: String = "",
    val validLabel: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
