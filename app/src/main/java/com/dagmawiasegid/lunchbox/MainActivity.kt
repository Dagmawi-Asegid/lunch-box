package com.dagmawiasegid.lunchbox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagmawiasegid.lunchbox.data.OverpassRepository
import com.dagmawiasegid.lunchbox.data.PromotionRepository
import com.dagmawiasegid.lunchbox.data.Restaurant
import com.dagmawiasegid.lunchbox.data.RestaurantRepository
import com.dagmawiasegid.lunchbox.data.SortOption
import com.dagmawiasegid.lunchbox.databinding.ActivityMainBinding
import com.dagmawiasegid.lunchbox.ui.QuickFilter
import com.dagmawiasegid.lunchbox.ui.RestaurantViewModel
import com.dagmawiasegid.lunchbox.ui.adapter.RestaurantAdapter
import com.dagmawiasegid.lunchbox.util.DealsNotifier
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: RestaurantViewModel
    private lateinit var adapter: RestaurantAdapter
    private val restaurantRepository = RestaurantRepository()
    private val overpassRepository = OverpassRepository()
    private val promotionRepository = PromotionRepository()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Re-check deals once we actually have permission — the initial
            // check in onCreate() runs before the user has answered this
            // prompt, so the notification would silently no-op on first launch.
            if (granted) checkForDeals()
        }

    private val sortLabels = listOf("Top rated", "Nearest", "Name (A-Z)", "Location")

    // Default center shown before we know the user's real location (Vassar
    // College, Poughkeepsie NY — where this app's seed data is centered).
    private val defaultCenter = GeoPoint(41.6906, -73.8990)

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNearbyRestaurants()
            } else {
                binding.nearbyStatus.text =
                    "Location permission denied — showing saved restaurants only. Tap ⟳ to try again."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupMap()

        viewModel = ViewModelProvider(this)[RestaurantViewModel::class.java]

        adapter = RestaurantAdapter(
            onReviewClick = { restaurant ->
                val intent = Intent(this, AddReviewActivity::class.java)
                    .putExtra(AddReviewActivity.EXTRA_RESTAURANT_ID, restaurant.id)
                    .putExtra(AddReviewActivity.EXTRA_RESTAURANT_NAME, restaurant.name)
                startActivity(intent)
            },
            onDirectionsClick = { restaurant -> openDirections(restaurant) },
            onOrderClick = { restaurant ->
                val intent = Intent(this, OrderActivity::class.java)
                    .putExtra(OrderActivity.EXTRA_RESTAURANT_ID, restaurant.id)
                    .putExtra(OrderActivity.EXTRA_RESTAURANT_NAME, restaurant.name)
                startActivity(intent)
            },
            getUserLocation = { viewModel.userLocation }
        )
        binding.restaurantList.layoutManager = LinearLayoutManager(this)
        binding.restaurantList.adapter = adapter

        binding.sortSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, sortLabels
        )
        binding.sortSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val sort = when (position) {
                    1 -> SortOption.DISTANCE
                    2 -> SortOption.NAME
                    3 -> SortOption.LOCATION
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

        binding.swipeRefresh.setOnRefreshListener {
            requestLocationAndSearchNearby()
        }

        setupQuickFilters()

        viewModel.restaurants.observe(this) { list ->
            adapter.submitList(list)
            updateMapMarkers(list)
        }
        viewModel.error.observe(this) { message ->
            binding.errorText.visibility = if (message == null) android.view.View.GONE else android.view.View.VISIBLE
            binding.errorText.text = message
        }

        viewModel.load()
        requestLocationAndSearchNearby()
        requestNotificationPermission()
        checkForDeals()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        // Aggregate ratings may have changed if the user just submitted a review.
        viewModel.load()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menu.add("🎉 Deals near you").setOnMenuItemClickListener {
            startActivity(Intent(this, DealsActivity::class.java))
            true
        }
        menu.add("Find restaurants near me").setOnMenuItemClickListener {
            requestLocationAndSearchNearby()
            true
        }
        menu.add("Log out").setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            true
        }
        return true
    }

    private fun setupQuickFilters() {
        val chips = mapOf(
            binding.filterAll to QuickFilter.ALL,
            binding.filterBurgers to QuickFilter.BURGERS_AND_FAST_FOOD,
            binding.filterCafes to QuickFilter.CAFES,
            binding.filterTopRated to QuickFilter.REVIEWED
        )
        chips.forEach { (button, filter) ->
            button.setOnClickListener {
                viewModel.setQuickFilter(filter)
                chips.keys.forEach { b ->
                    b.setBackgroundResource(
                        if (b == button) R.drawable.bg_chip_selected else R.drawable.bg_chip
                    )
                    b.setTextColor(
                        resources.getColor(
                            if (b == button) android.R.color.white else android.R.color.black,
                            theme
                        )
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkForDeals() {
        lifecycleScope.launch {
            try {
                val deals = promotionRepository.fetchPromotions()
                if (deals.isNotEmpty()) {
                    DealsNotifier.notifyNewDeals(this@MainActivity, deals.size)
                }
            } catch (e: Exception) {
                // Deals are a nice-to-have; failing silently here is fine —
                // the Deals menu item will show the real error if the user opens it.
            }
        }
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(14.0)
        binding.mapView.controller.setCenter(defaultCenter)
    }

    private fun requestLocationAndSearchNearby() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            findNearbyRestaurants()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun findNearbyRestaurants() {
        binding.nearbyStatus.text = "Finding restaurants near you…"
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launch {
            val location = try {
                kotlinx.coroutines.withTimeoutOrNull(15_000) {
                    fusedClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    ).await()
                }
            } catch (e: Exception) {
                null
            }

            binding.swipeRefresh.isRefreshing = false

            if (location == null) {
                binding.nearbyStatus.text =
                    "Couldn't get your location — showing saved restaurants only. Tap ⟳ to try again."
                return@launch
            }
            viewModel.setUserLocation(location.latitude, location.longitude)
            binding.mapView.controller.animateTo(GeoPoint(location.latitude, location.longitude))
            syncNearbyRestaurants(location.latitude, location.longitude)
        }
    }

    private fun syncNearbyRestaurants(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val nearby = overpassRepository.searchNearby(latitude, longitude)
                nearby.forEach { place -> restaurantRepository.upsertFromPlace(place) }
                binding.nearbyStatus.text = if (nearby.isEmpty()) {
                    "No restaurants found via OpenStreetMap nearby — showing saved list."
                } else {
                    "Found ${nearby.size} restaurants near you (OpenStreetMap). Tap a pin or card to explore."
                }
                viewModel.load()
            } catch (e: Exception) {
                binding.nearbyStatus.text = "Couldn't load nearby restaurants (${e.message}). Showing saved list."
            }
        }
    }

    private fun updateMapMarkers(restaurants: List<Restaurant>) {
        binding.mapView.overlays.clear()

        viewModel.userLocation?.let { (lat, lon) ->
            val userMarker = Marker(binding.mapView)
            userMarker.position = GeoPoint(lat, lon)
            userMarker.title = "You"
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            binding.mapView.overlays.add(userMarker)
        }

        restaurants.forEach { restaurant ->
            val lat = restaurant.latitude ?: return@forEach
            val lon = restaurant.longitude ?: return@forEach
            val marker = Marker(binding.mapView)
            marker.position = GeoPoint(lat, lon)
            marker.title = restaurant.name
            marker.snippet = restaurant.cuisine
            marker.setOnMarkerClickListener { clickedMarker, _ ->
                val index = restaurants.indexOf(restaurant)
                if (index >= 0) binding.restaurantList.smoothScrollToPosition(index)
                clickedMarker.showInfoWindow()
                true
            }
            binding.mapView.overlays.add(marker)
        }

        binding.mapView.invalidate()
    }

    private fun openDirections(restaurant: Restaurant) {
        val lat = restaurant.latitude ?: return
        val lng = restaurant.longitude ?: return
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
