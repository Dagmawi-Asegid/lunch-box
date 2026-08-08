package com.dagmawiasegid.lunchbox

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagmawiasegid.lunchbox.data.SortOption
import com.dagmawiasegid.lunchbox.databinding.ActivityMainBinding
import com.dagmawiasegid.lunchbox.ui.RestaurantViewModel
import com.dagmawiasegid.lunchbox.ui.adapter.RestaurantAdapter
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: RestaurantViewModel
    private lateinit var adapter: RestaurantAdapter

    private val sortLabels = listOf("Top rated", "Name (A-Z)", "Location")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[RestaurantViewModel::class.java]

        adapter = RestaurantAdapter { restaurant ->
            val intent = Intent(this, AddReviewActivity::class.java)
                .putExtra(AddReviewActivity.EXTRA_RESTAURANT_ID, restaurant.id)
                .putExtra(AddReviewActivity.EXTRA_RESTAURANT_NAME, restaurant.name)
            startActivity(intent)
        }
        binding.restaurantList.layoutManager = LinearLayoutManager(this)
        binding.restaurantList.adapter = adapter

        binding.sortSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, sortLabels
        )
        binding.sortSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val sort = when (position) {
                    1 -> SortOption.NAME
                    2 -> SortOption.LOCATION
                    else -> SortOption.RATING
                }
                viewModel.load(sortBy = sort)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.restaurants.observe(this) { adapter.submitList(it) }
        viewModel.error.observe(this) { message ->
            binding.errorText.visibility = if (message == null) android.view.View.GONE else android.view.View.VISIBLE
            binding.errorText.text = message
        }

        viewModel.load()
    }

    override fun onResume() {
        super.onResume()
        // Aggregate ratings may have changed if the user just submitted a review.
        viewModel.load()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menu.add("Log out").setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            true
        }
        return true
    }
}
