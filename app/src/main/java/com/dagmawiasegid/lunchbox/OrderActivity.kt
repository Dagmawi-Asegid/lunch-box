package com.dagmawiasegid.lunchbox

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dagmawiasegid.lunchbox.data.DemoMenu
import com.dagmawiasegid.lunchbox.data.Order
import com.dagmawiasegid.lunchbox.data.OrderLineItem
import com.dagmawiasegid.lunchbox.data.OrderRepository
import com.dagmawiasegid.lunchbox.databinding.ActivityOrderBinding
import com.dagmawiasegid.lunchbox.databinding.ItemMenuLineBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var restaurantId: String
    private lateinit var restaurantName: String

    private val quantities = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restaurantId = intent.getStringExtra(EXTRA_RESTAURANT_ID) ?: run {
            finish()
            return
        }
        restaurantName = intent.getStringExtra(EXTRA_RESTAURANT_NAME).orEmpty()
        binding.orderRestaurantName.text = restaurantName

        buildMenu()
        binding.placeOrderButton.setOnClickListener { placeOrder() }
    }

    private fun buildMenu() {
        DemoMenu.items.forEach { (name, price) ->
            val row = ItemMenuLineBinding.inflate(LayoutInflater.from(this), binding.menuContainer, false)
            row.menuItemName.text = name
            row.menuItemPrice.text = String.format(Locale.US, "$%.2f", price)
            quantities[name] = 0

            row.increaseButton.setOnClickListener {
                quantities[name] = (quantities[name] ?: 0) + 1
                row.quantityText.text = quantities[name].toString()
                updateTotal()
            }
            row.decreaseButton.setOnClickListener {
                val current = quantities[name] ?: 0
                if (current > 0) {
                    quantities[name] = current - 1
                    row.quantityText.text = quantities[name].toString()
                    updateTotal()
                }
            }
            binding.menuContainer.addView(row.root)
        }
    }

    private fun currentLineItems(): List<OrderLineItem> =
        DemoMenu.items.mapNotNull { (name, price) ->
            val qty = quantities[name] ?: 0
            if (qty > 0) OrderLineItem(name = name, price = price, quantity = qty) else null
        }

    private fun updateTotal() {
        val total = currentLineItems().sumOf { it.price * it.quantity }
        binding.orderTotal.text = String.format(Locale.US, "Total: $%.2f", total)
    }

    private fun placeOrder() {
        val items = currentLineItems()
        val user = auth.currentUser

        if (items.isEmpty()) {
            showError("Add at least one item to your order")
            return
        }
        if (user == null) {
            showError("You must be logged in to order")
            return
        }

        binding.placeOrderButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val total = items.sumOf { it.price * it.quantity }
                orderRepository.placeOrder(
                    Order(
                        restaurantId = restaurantId,
                        restaurantName = restaurantName,
                        userId = user.uid,
                        items = items,
                        total = total,
                        notes = binding.notesInput.text?.toString().orEmpty()
                    )
                )
                binding.orderError.visibility = android.view.View.GONE
                binding.placeOrderButton.visibility = android.view.View.GONE
                binding.orderConfirmation.visibility = android.view.View.VISIBLE
                binding.orderConfirmation.text =
                    "✓ Order placed (demo) — ${items.sumOf { it.quantity }} item(s), " +
                        String.format(Locale.US, "$%.2f total.", total)
            } catch (e: Exception) {
                showError(e.message ?: "Failed to place order")
                binding.placeOrderButton.isEnabled = true
            }
        }
    }

    private fun showError(message: String) {
        binding.orderError.text = message
        binding.orderError.visibility = android.view.View.VISIBLE
    }

    companion object {
        const val EXTRA_RESTAURANT_ID = "extra_restaurant_id"
        const val EXTRA_RESTAURANT_NAME = "extra_restaurant_name"
    }
}
