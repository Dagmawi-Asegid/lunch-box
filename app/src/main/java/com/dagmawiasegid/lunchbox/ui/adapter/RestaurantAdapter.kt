package com.dagmawiasegid.lunchbox.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dagmawiasegid.lunchbox.data.Restaurant
import com.dagmawiasegid.lunchbox.databinding.ItemRestaurantBinding
import com.dagmawiasegid.lunchbox.util.CuisineIcons
import com.dagmawiasegid.lunchbox.util.CuisinePhotos
import com.dagmawiasegid.lunchbox.util.DistanceUtil
import com.dagmawiasegid.lunchbox.util.LandmarkContext

class RestaurantAdapter(
    private val onReviewClick: (Restaurant) -> Unit,
    private val onDirectionsClick: (Restaurant) -> Unit,
    private val onOrderClick: (Restaurant) -> Unit,
    private val getUserLocation: () -> Pair<Double, Double>?
) : ListAdapter<Restaurant, RestaurantAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(restaurant: Restaurant) {
            binding.restaurantName.text = restaurant.name
            binding.restaurantLocation.text = restaurant.location
            binding.restaurantCuisine.text = restaurant.cuisine
            binding.restaurantRating.text = if (restaurant.reviewCount > 0) {
                String.format("%.1f ★ (%d)", restaurant.averageRating, restaurant.reviewCount)
            } else {
                "No reviews yet — be the first"
            }

            binding.cuisineEmoji.text = CuisineIcons.forCuisine(restaurant.cuisine)

            // Prefer the restaurant's own real photo (rare — only when OSM
            // has one); otherwise fall back to a real, freely-licensed
            // photo representative of the cuisine; otherwise the emoji
            // banner as a last resort.
            val bannerUrl = restaurant.photoUrl ?: CuisinePhotos.forCuisine(restaurant.cuisine)
            if (bannerUrl != null) {
                binding.restaurantPhoto.visibility = View.VISIBLE
                binding.photoPlaceholder.visibility = View.GONE
                binding.cuisineEmoji.visibility = View.GONE
                Glide.with(binding.restaurantPhoto).load(bannerUrl).into(binding.restaurantPhoto)
            } else {
                binding.restaurantPhoto.visibility = View.GONE
                binding.photoPlaceholder.visibility = View.VISIBLE
                binding.cuisineEmoji.visibility = View.VISIBLE
            }

            val userLocation = getUserLocation()
            val lat = restaurant.latitude
            val lon = restaurant.longitude
            if (userLocation != null && lat != null && lon != null) {
                val meters = DistanceUtil.metersBetween(userLocation.first, userLocation.second, lat, lon)
                binding.distanceBadge.visibility = View.VISIBLE
                binding.distanceBadge.text = DistanceUtil.formatMiles(meters)
            } else {
                binding.distanceBadge.visibility = View.GONE
            }

            val landmark = if (lat != null && lon != null) LandmarkContext.forLocation(lat, lon) else null
            if (landmark != null) {
                binding.landmarkContext.visibility = View.VISIBLE
                binding.landmarkContext.text = landmark
            } else {
                binding.landmarkContext.visibility = View.GONE
            }

            val hasLocation = lat != null && lon != null
            binding.directionsButton.visibility = if (hasLocation) View.VISIBLE else View.GONE
            binding.directionsButton.setOnClickListener { onDirectionsClick(restaurant) }

            binding.orderButton.setOnClickListener { onOrderClick(restaurant) }
            binding.reviewButton.setOnClickListener { onReviewClick(restaurant) }
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
