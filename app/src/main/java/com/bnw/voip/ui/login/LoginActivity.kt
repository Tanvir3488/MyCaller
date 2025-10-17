package com.bnw.voip.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bnw.voip.databinding.ActivityLoginBinding
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.ui.main.MainActivity
import com.bnw.voip.utils.AppConstants
import com.bnw.voip.voip.CallState
import com.bnw.voip.voip.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var getCallStateUseCase: GetCallStateUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.passwordEditText.setText(Constants.PASSWORD)
        binding.usernameEditText.setText(Constants.USERNAME)
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(username, password)
        }

        lifecycleScope.launch {
            getCallStateUseCase().collect { state ->
                when (val registrationState = state.registrationState) {
                    is CallState.RegistrationState.Progress -> {
                        // Show progress
                    }
                    is CallState.RegistrationState.Ok -> {
                        navigateToMain()
                    }
                    is CallState.RegistrationState.Failed -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Registration failed: ${registrationState.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
