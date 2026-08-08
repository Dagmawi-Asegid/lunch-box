package com.dagmawiasegid.lunchbox.data

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val cuisine: String = "",
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0
)
