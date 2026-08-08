package com.dagmawiasegid.lunchbox.util

object CuisineIcons {
    private val map = mapOf(
        "italian" to "🍝",
        "pizza" to "🍕",
        "chinese" to "🥡",
        "japanese" to "🍱",
        "sushi" to "🍣",
        "mexican" to "🌮",
        "indian" to "🍛",
        "thai" to "🍜",
        "vietnamese" to "🍜",
        "american" to "🍔",
        "burger" to "🍔",
        "bbq" to "🍖",
        "seafood" to "🦐",
        "breakfast" to "🥞",
        "cafe" to "☕",
        "coffee shop" to "☕",
        "sandwich" to "🥪",
        "korean" to "🍲",
        "mediterranean" to "🥙",
        "greek" to "🥙",
        "french" to "🥐",
        "bakery" to "🥐",
        "vegan" to "🥗",
        "vegetarian" to "🥗",
        "steak house" to "🥩",
        "chicken" to "🍗",
        "ice cream" to "🍦",
        "fish and chips" to "🐟",
        "regional" to "🍽️"
    )

    fun forCuisine(cuisine: String): String =
        map[cuisine.trim().lowercase()] ?: "🍽️"
}
