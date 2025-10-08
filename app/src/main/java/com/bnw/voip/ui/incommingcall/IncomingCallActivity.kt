package com.bnw.voip.ui.incommingcall

import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bnw.voip.MyApplication.Companion.sipManager
import com.bnw.voip.R
import com.bnw.voip.databinding.ActivityIncomingCallBinding
import com.bnw.voip.voip.CustomeSipManager

class IncomingCallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomingCallBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show caller number dynamically if passed via intent
        val caller = intent.getStringExtra("caller") ?: "+880123456789"
        binding.tvCaller.text = "Incoming call: $caller"

        binding.btnAccept.setOnClickListener {
            sipManager.answerCall()
            binding.tvTimer.base = SystemClock.elapsedRealtime()
            binding.tvTimer.visibility = View.VISIBLE
            binding.tvTimer.start()   // start counting up
        }

        binding.btnDecline.setOnClickListener {
            finish() // end call UI
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
