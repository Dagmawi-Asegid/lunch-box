package com.dagmawiasegid.lunchbox.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagmawiasegid.lunchbox.data.Restaurant
import com.dagmawiasegid.lunchbox.data.RestaurantRepository
import com.dagmawiasegid.lunchbox.data.SortOption
import com.dagmawiasegid.lunchbox.util.DistanceUtil
import kotlinx.coroutines.launch

enum class QuickFilter { ALL, BURGERS_AND_FAST_FOOD, CAFES, REVIEWED }

class RestaurantViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    private val _restaurants = MutableLiveData<List<Restaurant>>(emptyList())
    val restaurants: LiveData<List<Restaurant>> = _restaurants

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // The last full (unfiltered) list fetched from Firestore, so search can
    // filter client-side instead of re-querying — this makes search
    // case-insensitive and substring-based, not just a case-sensitive
    // prefix match (Firestore range queries compare bytes, so a
    // lowercase query would never match a capitalized name).
    private var lastLoaded: List<Restaurant> = emptyList()
    private var currentQuery: String = ""
    private var currentFilter: QuickFilter = QuickFilter.ALL

    // Set once we know the user's location, so DISTANCE sort can re-order
    // the already-loaded list client-side (Firestore can't sort by
    // distance-from-a-point without geohash indexing, which is overkill
    // for this app's scale).
    var userLocation: Pair<Double, Double>? = null
        private set

    var currentSort: SortOption = SortOption.RATING
        private set

    fun load(sortBy: SortOption = currentSort, locationFilter: String? = null) {
        currentSort = sortBy
        viewModelScope.launch {
            try {
                lastLoaded = repository.fetchRestaurants(sortBy, locationFilter)
                _restaurants.value = present(lastLoaded)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load restaurants"
            }
        }
    }

    fun search(query: String) {
        currentQuery = query
        _restaurants.value = present(lastLoaded)
    }

    fun setQuickFilter(filter: QuickFilter) {
        currentFilter = filter
        _restaurants.value = present(lastLoaded)
    }

    fun setUserLocation(latitude: Double, longitude: Double) {
        userLocation = latitude to longitude
        if (currentSort == SortOption.DISTANCE) {
            _restaurants.value = present(lastLoaded)
        }
    }

    private fun present(list: List<Restaurant>): List<Restaurant> {
        var result = if (currentQuery.isBlank()) {
            list
        } else {
            list.filter { it.name.contains(currentQuery, ignoreCase = true) }
        }

        result = when (currentFilter) {
            QuickFilter.ALL -> result
            QuickFilter.BURGERS_AND_FAST_FOOD -> result.filter { isBurgerOrFastFood(it) }
            QuickFilter.CAFES -> result.filter { isCafe(it) }
            QuickFilter.REVIEWED -> result.filter { it.reviewCount > 0 }
        }

        if (currentSort != SortOption.DISTANCE) return result
        val loc = userLocation ?: return result
        return result.sortedBy { restaurant ->
            val lat = restaurant.latitude
            val lon = restaurant.longitude
            if (lat != null && lon != null) {
                DistanceUtil.metersBetween(loc.first, loc.second, lat, lon)
            } else {
                Double.MAX_VALUE
            }
        }
    }

    private fun isBurgerOrFastFood(restaurant: Restaurant): Boolean {
        if (restaurant.amenityType == "fast_food") return true
        val cuisine = restaurant.cuisine.lowercase()
        if (cuisine.contains("burger") || cuisine.contains("fast food")) return true
        val name = restaurant.name.lowercase()
        return KNOWN_FAST_FOOD_CHAINS.any { name.contains(it) }
    }

    private fun isCafe(restaurant: Restaurant): Boolean {
        if (restaurant.amenityType == "cafe") return true
        val cuisine = restaurant.cuisine.lowercase()
        return cuisine.contains("cafe") || cuisine.contains("coffee") || cuisine.contains("bakery")
    }

    companion object {
        private val KNOWN_FAST_FOOD_CHAINS = listOf(
            "mcdonald", "burger king", "wendy", "popeyes", "five guys", "shake shack",
            "taco bell", "kfc", "chick-fil-a", "chick fil a", "sonic", "culver",
            "white castle", "checkers", "rally's", "in-n-out", "whataburger",
            "carl's jr", "hardee", "jack in the box", "arby"
        )
    }
}
