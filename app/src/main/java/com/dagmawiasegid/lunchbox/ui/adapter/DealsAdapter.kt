package com.dagmawiasegid.lunchbox.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dagmawiasegid.lunchbox.data.Promotion
import com.dagmawiasegid.lunchbox.databinding.ItemDealBinding

class DealsAdapter : ListAdapter<Promotion, DealsAdapter.ViewHolder>(DIFF) {

    class ViewHolder(private val binding: ItemDealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(promo: Promotion) {
            binding.dealTitle.text = promo.title
            binding.dealRestaurant.text = promo.restaurantName
            binding.dealDescription.text = promo.description
            binding.dealValid.text = promo.validLabel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Promotion>() {
            override fun areItemsTheSame(oldItem: Promotion, newItem: Promotion) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Promotion, newItem: Promotion) = oldItem == newItem
        }
    }
}
