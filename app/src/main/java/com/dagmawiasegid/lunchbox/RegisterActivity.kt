package com.dagmawiasegid.lunchbox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dagmawiasegid.lunchbox.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        if (email.isBlank() || password.length < 6) {
            showError("Enter a valid email and a password with at least 6 characters")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { showError(it.message ?: "Registration failed") }
    }

    private fun showError(message: String) {
        binding.registerError.text = message
        binding.registerError.visibility = android.view.View.VISIBLE
    }
}
