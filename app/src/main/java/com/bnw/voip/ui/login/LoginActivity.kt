package com.bnw.voip.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.bnw.voip.R
import com.bnw.voip.databinding.ActivityLoginBinding
import com.bnw.voip.ui.main.MainActivity
import com.bnw.voip.voip.CallState
import com.bnw.voip.voip.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeLoginState()
    }
    
    private fun setupUI() {
        // Pre-fill credentials
        binding.passwordEditText.setText(Constants.PASSWORD)
        binding.usernameEditText.setText(Constants.USERNAME)
        
        // Set up login button click listener
        binding.loginButton.setOnClickListener {
            performLogin()
        }
        
        // Set up password toggle functionality
        binding.passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
        
        // Set up sign up text view click listener (placeholder for now)
        binding.signupTextView.setOnClickListener {
            Toast.makeText(this, "Sign up functionality not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            isPasswordVisible = false
        } else {
            // Show password
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.passwordToggle.setImageResource(R.drawable.ic_visibility)
            isPasswordVisible = true
        }
        // Move cursor to end
        binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
    }
    
    private fun performLogin() {
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        
        // Basic validation
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            binding.usernameEditText.requestFocus()
            return
        }
        
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            binding.passwordEditText.requestFocus()
            return
        }
        
        // Start login process
        viewModel.login(username, password)
    }
    
    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.callState.take(3).collect { state ->
                when (val registrationState = state.registrationState) {
                    is CallState.RegistrationState.Progress -> {
                        showProgress(true)
                    }
                    is CallState.RegistrationState.Ok -> {
                        showProgress(false)
                        navigateToMain()
                    }
                    is CallState.RegistrationState.Failed -> {
                        showProgress(false)
                        showError("Login failed: ${registrationState.message}")
                    }
                    else -> {
                        showProgress(false)
                    }
                }
            }
        }
    }
    
    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
        binding.loginButton.text = if (show) "Signing In..." else "Sign In"
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        Log.e("LoginActivity", "navigateToMain")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
