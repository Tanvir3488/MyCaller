package com.bnw.voip.ui.incommingcall

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            viewModel.answerCall()
        }

        binding.btnDecline.setOnClickListener {
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
            viewModel.uiState.collect { uiState ->
                binding.btnAccept.visibility = if (uiState.isAcceptButtonVisible) View.VISIBLE else View.GONE
                binding.btnDecline.text = uiState.declineButtonText
            }
        }

        viewModel.callState.observe(this) { state ->
            Log.e("CallingActivity:", "Call $state")
            when (state?.state) {
                Call.State.Connected, Call.State.StreamsRunning -> {
                    binding.tvTimer.base = SystemClock.elapsedRealtime()
                    binding.tvTimer.visibility = View.VISIBLE
                    binding.tvTimer.start()
                    viewModel.showOngoingCallNotification()
                }
                Call.State.End -> {
                    finish()
                }
                Call.State.Released -> {
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
    }
}
