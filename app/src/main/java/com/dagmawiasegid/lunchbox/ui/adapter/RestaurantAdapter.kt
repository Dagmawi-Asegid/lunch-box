package com.dagmawiasegid.lunchbox.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dagmawiasegid.lunchbox.data.Restaurant
import com.dagmawiasegid.lunchbox.databinding.ItemRestaurantBinding

class RestaurantAdapter(
    private val onClick: (Restaurant) -> Unit
) : ListAdapter<Restaurant, RestaurantAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(restaurant: Restaurant) {
            binding.restaurantName.text = restaurant.name
            binding.restaurantLocation.text = restaurant.location
            binding.restaurantCuisine.text = restaurant.cuisine
            binding.restaurantRating.text = String.format("%.1f ★ (%d)", restaurant.averageRating, restaurant.reviewCount)
            binding.root.setOnClickListener { onClick(restaurant) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Restaurant>() {
            override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant) = oldItem == newItem
        }
    }
}
