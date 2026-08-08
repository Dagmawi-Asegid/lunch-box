package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val orders = firestore.collection("orders")

    suspend fun placeOrder(order: Order): String {
        val ref = orders.add(order).await()
        return ref.id
    }
}
