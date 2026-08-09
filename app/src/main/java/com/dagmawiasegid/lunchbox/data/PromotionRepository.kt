package com.dagmawiasegid.lunchbox.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PromotionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val promotions = firestore.collection("promotions")

    suspend fun fetchPromotions(): List<Promotion> {
        return promotions
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Promotion::class.java)
    }
}
