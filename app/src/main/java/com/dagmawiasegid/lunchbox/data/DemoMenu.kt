package com.dagmawiasegid.lunchbox.data

/**
 * Google Places doesn't provide structured menu data, and there's no real
 * point-of-sale system behind this app — so this is a generic, clearly
 * illustrative menu shown for every restaurant, not scraped/real items.
 */
object DemoMenu {
    val items = listOf(
        "Signature Entrée" to 14.99,
        "Soup or Salad" to 6.49,
        "Side Dish" to 4.99,
        "Fountain Drink" to 2.49,
        "Dessert" to 5.99
    )
}
