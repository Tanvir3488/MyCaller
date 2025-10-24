package com.bnw.voip.ui.incommingcall

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.bnw.voip.R
import com.bnw.voip.databinding.ActivityIncomingCallBinding
import com.bnw.voip.utils.AppConstants
import com.bnw.voip.voip.CallState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomingCallBinding
    private val viewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge and secure window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupAnimations()
        Log.e("CallingActivity:", "onCreate ${intent?.action}")
        when (intent?.action) {
            AppConstants.ACTION_ANSWER_CALL -> {
                viewModel.answerCall()
            }
            AppConstants.ACTION_DECLINE_CALL -> {
                viewModel.hangupCall()
            }
        }

        binding.btnAccept.setOnClickListener {
            Log.e("CallingActivity:", "Answer Call Clicked")
            animateButtonPress(it)
            viewModel.answerCall()
        }

        binding.btnDecline.setOnClickListener {
            Log.e("CallingActivity:", "Hung up Call Clicked")
            animateButtonPress(it)
            viewModel.hangupCall()
        }

        lifecycleScope.launch {
            viewModel.callerName.collect { name ->
                binding.tvCallerName.text = name
            }
        }

        lifecycleScope.launch {
            viewModel.callerNumber.collect { number ->
                binding.tvCallerNumber.text = number
            }
        }

        lifecycleScope.launch {
            viewModel.photoUri.collect { photoUri ->
                if (photoUri != null) {
                    binding.ivAvatar.load(photoUri) {
                        crossfade(true)
                        placeholder(R.drawable.ic_profile)
                        error(R.drawable.ic_profile)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    binding.ivAvatar.load(R.drawable.ic_profile) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                Log.e("CallingActivityTTT:", "UI State Updated: ${uiState.isAcceptButtonVisible}")
                binding.btnAccept.visibility = if (uiState.isAcceptButtonVisible) View.VISIBLE else View.GONE
              //  binding.btnDecline.text = uiState.declineButtonText
            }
        }

        lifecycleScope.launch {
            viewModel.callState.collect { state ->
                Log.e("CallingActivity:", "Call $state")
                when (state.callState) {
                    is CallState.Connected -> {
                        viewModel.callConnectedTime.value?.let {
                            binding.tvTimer.base = it
                            binding.timerCard.visibility = View.VISIBLE
                            binding.tvTimer.start()
                            binding.tvCallStatus.text = "Connected"
                            viewModel.showOngoingCallNotification()
                        }
                    }
                    is CallState.Released -> {
                        finish()
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.callConnectedTime.collect { time ->
                if (time != null) {
                    binding.tvTimer.base = time
                    binding.timerCard.visibility = View.VISIBLE
                    binding.tvTimer.start()
                    binding.tvCallStatus.text = "Connected"
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        when (intent.action) {
            AppConstants.ACTION_ANSWER_CALL -> {
                viewModel.answerCall()
            }
            AppConstants.ACTION_DECLINE_CALL -> {
                viewModel.hangupCall()
            }
        }
    }



    private fun setupAnimations() {
        // Animate avatar entrance
        binding.avatarCard.alpha = 0f
        binding.avatarCard.scaleX = 0.5f
        binding.avatarCard.scaleY = 0.5f
        
        binding.avatarCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .start()
        
        // Animate call info card entrance
        binding.callInfoCard.alpha = 0f
        binding.callInfoCard.translationY = 50f
        
        binding.callInfoCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(200)
            .start()
        
        // Animate action buttons entrance
        binding.actionButtonsLayout.alpha = 0f
        binding.actionButtonsLayout.translationY = 100f
        
        binding.actionButtonsLayout.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(400)
            .start()
    }
    
    private fun animateButtonPress(view: View) {
        val scaleAnimation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f)
            )
            duration = 150
        }
        scaleAnimation.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}
