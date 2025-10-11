package com.bnw.voip.ui.incommingcall

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bnw.voip.databinding.ActivityIncomingCallBinding
import com.bnw.voip.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.linphone.core.Call

@AndroidEntryPoint
class CallingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomingCallBinding
    private val viewModel: IncomingCallViewModel by viewModels()
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
            viewModel.answerCall()
        }

        binding.btnDecline.setOnClickListener {
            Log.e("CallingActivity:", "Hung up Call Clicked")
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
                    binding.ivAvatar.setImageURI(android.net.Uri.parse(photoUri))
                } else {
                    binding.ivAvatar.setImageResource(com.bnw.voip.R.drawable.ic_launcher_foreground) // Or a placeholder
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

        viewModel.callState.observe(this) { state ->
            Log.e("CallingActivity:", "Call $state")
            when (state?.state) {
                Call.State.IncomingReceived -> {
                    val pattern = longArrayOf(0, 1000, 500) // Vibrate for 1s, pause for 0.5s
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)) // Repeat from index 0
                    } else {
                        vibrator.vibrate(pattern, 0) // Repeat from index 0
                    }
                }
                Call.State.Connected, Call.State.StreamsRunning -> {
                    vibrator.cancel()
                    binding.tvTimer.base = SystemClock.elapsedRealtime()
                    binding.tvTimer.visibility = View.VISIBLE
                    binding.tvTimer.start()
                    viewModel.showOngoingCallNotification()
                }
                Call.State.End -> {
                    vibrator.cancel()
                    finish()
                }
                Call.State.Released -> {
                    vibrator.cancel()
                    finish()
                }
                else -> {
                    // Handle other states if needed
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



    override fun onDestroy() {
        super.onDestroy()
        vibrator.cancel()
    }
}
