package com.dagmawiasegid.lunchbox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dagmawiasegid.lunchbox.data.Review
import com.dagmawiasegid.lunchbox.data.RestaurantRepository
import com.dagmawiasegid.lunchbox.databinding.ActivityAddReviewBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AddReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReviewBinding
    private val repository = RestaurantRepository()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var restaurantId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restaurantId = intent.getStringExtra(EXTRA_RESTAURANT_ID) ?: run {
            finish()
            return
        }
        binding.reviewRestaurantName.text = intent.getStringExtra(EXTRA_RESTAURANT_NAME).orEmpty()

        binding.submitReviewButton.setOnClickListener { submit() }
    }

    private fun submit() {
        val rating = binding.ratingBar.rating
        val comment = binding.commentInput.text?.toString()?.trim().orEmpty()
        val user = auth.currentUser

        if (rating <= 0f) {
            showError("Please select a star rating")
            return
        }
        if (user == null) {
            showError("You must be logged in to review")
            return
        }

        binding.submitReviewButton.isEnabled = false
        lifecycleScope.launch {
            try {
                repository.submitReview(
                    restaurantId,
                    Review(
                        restaurantId = restaurantId,
                        userId = user.uid,
                        userEmail = user.email.orEmpty(),
                        rating = rating,
                        comment = comment
                    )
                )
                finish()
            } catch (e: Exception) {
                showError(e.message ?: "Failed to submit review")
                binding.submitReviewButton.isEnabled = true
            }
        }
    }

    private fun showError(message: String) {
        binding.submitError.text = message
        binding.submitError.visibility = android.view.View.VISIBLE
    }

    companion object {
        const val EXTRA_RESTAURANT_ID = "extra_restaurant_id"
        const val EXTRA_RESTAURANT_NAME = "extra_restaurant_name"
    }
}
