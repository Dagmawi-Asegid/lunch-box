package com.dagmawiasegid.lunchbox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagmawiasegid.lunchbox.data.PromotionRepository
import com.dagmawiasegid.lunchbox.databinding.ActivityDealsBinding
import com.dagmawiasegid.lunchbox.ui.adapter.DealsAdapter
import kotlinx.coroutines.launch

class DealsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDealsBinding
    private val promotionRepository = PromotionRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = DealsAdapter()
        binding.dealsList.layoutManager = LinearLayoutManager(this)
        binding.dealsList.adapter = adapter

        lifecycleScope.launch {
            try {
                val deals = promotionRepository.fetchPromotions()
                adapter.submitList(deals)
                binding.emptyState.visibility = if (deals.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            } catch (e: Exception) {
                binding.emptyState.visibility = android.view.View.VISIBLE
                binding.emptyState.text = "Couldn't load deals (${e.message})"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
