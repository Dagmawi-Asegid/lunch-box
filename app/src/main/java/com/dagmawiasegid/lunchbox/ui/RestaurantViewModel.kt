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

    var currentSort: SortOption = SortOption.RATING
        private set

    fun load(sortBy: SortOption = currentSort, locationFilter: String? = null) {
        currentSort = sortBy
        viewModelScope.launch {
            try {
                _restaurants.value = repository.fetchRestaurants(sortBy, locationFilter)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load restaurants"
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            load()
            return
        }
        viewModelScope.launch {
            try {
                _restaurants.value = repository.searchByName(query)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Search failed"
            }
        }
    }
}
