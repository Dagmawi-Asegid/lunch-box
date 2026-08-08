package com.dagmawiasegid.lunchbox.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagmawiasegid.lunchbox.data.Restaurant
import com.dagmawiasegid.lunchbox.data.RestaurantRepository
import com.dagmawiasegid.lunchbox.data.SortOption
import kotlinx.coroutines.launch

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

    var currentSort: SortOption = SortOption.RATING
        private set

    fun load(sortBy: SortOption = currentSort, locationFilter: String? = null) {
        currentSort = sortBy
        viewModelScope.launch {
            try {
                lastLoaded = repository.fetchRestaurants(sortBy, locationFilter)
                _restaurants.value = applyQuery(lastLoaded)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load restaurants"
            }
        }
    }

    fun search(query: String) {
        currentQuery = query
        _restaurants.value = applyQuery(lastLoaded)
    }

    private fun applyQuery(list: List<Restaurant>): List<Restaurant> {
        if (currentQuery.isBlank()) return list
        return list.filter { it.name.contains(currentQuery, ignoreCase = true) }
    }
}
