package com.bnw.voip.ui.incommingcall

import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bnw.voip.databinding.ActivityIncomingCallBinding
import dagger.hilt.android.AndroidEntryPoint
import org.linphone.core.Call

@AndroidEntryPoint
class IncomingCallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomingCallBinding
    private var countDownTimer: CountDownTimer? = null
    private val viewModel: IncomingCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show caller number dynamically if passed via intent
        val caller = intent.getStringExtra("caller") ?: "+880123456789"
        binding.tvCaller.text = "Incoming call: $caller"

        binding.btnAccept.setOnClickListener {
            viewModel.answerCall()
        }

        binding.btnDecline.setOnClickListener {
            viewModel.hangupCall()
        }

        viewModel.callState.observe(this) { state ->
            Log.e("Call State:","Call $state")
            when (state?.state) {
                Call.State.Connected, Call.State.StreamsRunning -> {
                    binding.tvTimer.base = SystemClock.elapsedRealtime()
                    binding.tvTimer.visibility = View.VISIBLE
                    binding.tvTimer.start()
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

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(60_000, 1000) { // 1 minute
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val sec = seconds % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, sec)
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
            }
        }.start()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
