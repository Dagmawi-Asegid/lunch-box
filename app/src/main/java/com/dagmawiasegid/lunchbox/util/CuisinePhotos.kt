package com.dagmawiasegid.lunchbox.util

/**
 * Real, freely-licensed food photography from Wikimedia Commons, matched
 * by cuisine type — used as a card banner when a restaurant doesn't have
 * its own photo (which is most of them; OSM rarely has restaurant photos).
 *
 * These are honestly *representative* of the cuisine, not a photo of what
 * that specific restaurant actually serves — there's no way to source real,
 * verified, restaurant-specific food photos without a paid photo API
 * (Google Places) or scraping a restaurant's own social media, neither of
 * which this app does.
 */
object CuisinePhotos {
    private val map = mapOf(
        "pizza" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Pizza-3007395.jpg/330px-Pizza-3007395.jpg",
        "sushi" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/60/Sushi_platter.jpg/330px-Sushi_platter.jpg",
        "japanese" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Shoyu_Ramen%EF%BC%88Tokyo_Ramen%EF%BC%89_-_01.jpg/330px-Shoyu_Ramen%EF%BC%88Tokyo_Ramen%EF%BC%89_-_01.jpg",
        "mexican" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/73/001_Tacos_de_carnitas%2C_carne_asada_y_al_pastor.jpg/330px-001_Tacos_de_carnitas%2C_carne_asada_y_al_pastor.jpg",
        "chicken" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Fried-Chicken-Set.jpg/330px-Fried-Chicken-Set.jpg",
        "burger" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Cheeseburger.jpg/330px-Cheeseburger.jpg",
        "american" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Fried_Oysters_%26_Collards_at_Drago%27s_Restaurant%2C_Metairie%2C_Louisiana.jpg/330px-Fried_Oysters_%26_Collards_at_Drago%27s_Restaurant%2C_Metairie%2C_Louisiana.jpg",
        "chinese" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Collage_Chinese_Cuisine_by_User-EME.png/330px-Collage_Chinese_Cuisine_by_User-EME.png",
        "vietnamese" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/2023-07-28_Cuisine_of_Hanoi_%E4%B8%80%E8%88%AC%E5%AE%B6%E5%BA%AD%E3%83%99%E3%83%88%E3%83%8A%E3%83%A0%E6%96%99%E7%90%86DSCF0706.jpg/330px-2023-07-28_Cuisine_of_Hanoi_%E4%B8%80%E8%88%AC%E5%AE%B6%E5%BA%AD%E3%83%99%E3%83%88%E3%83%8A%E3%83%A0%E6%96%99%E7%90%86DSCF0706.jpg",
        "thai" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/39/Phat_Thai_kung_Chang_Khien_street_stall.jpg/330px-Phat_Thai_kung_Chang_Khien_street_stall.jpg",
        "falafel" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Falafels_2.jpg/330px-Falafels_2.jpg",
        "mediterranean" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Falafels_2.jpg/330px-Falafels_2.jpg",
        "korean" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Han-jeongsik.jpg/330px-Han-jeongsik.jpg",
        "italian" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/Italian_food.JPG/330px-Italian_food.JPG",
        "indian" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Taj_Mahal_-_Lamb_Curry_Madras.jpg/330px-Taj_Mahal_-_Lamb_Curry_Madras.jpg",
        "cafe" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Caf%C3%A9_de_Flore.jpg/330px-Caf%C3%A9_de_Flore.jpg",
        "coffee shop" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Caf%C3%A9_de_Flore.jpg/330px-Caf%C3%A9_de_Flore.jpg",
        "bakery" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Belgium_2013_%2811620905224%29.jpg/330px-Belgium_2013_%2811620905224%29.jpg",
        "ice cream" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Ice_cream_with_whipped_cream%2C_chocolate_syrup%2C_and_a_wafer_%28cropped%29.jpg/330px-Ice_cream_with_whipped_cream%2C_chocolate_syrup%2C_and_a_wafer_%28cropped%29.jpg",
        "regional" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Collage_Mexican_Cuisine_by_User-EME.png/330px-Collage_Mexican_Cuisine_by_User-EME.png",
        "fast food" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Cheeseburger.jpg/330px-Cheeseburger.jpg"
    )

    fun forCuisine(cuisine: String): String? = map[cuisine.trim().lowercase()]
}
