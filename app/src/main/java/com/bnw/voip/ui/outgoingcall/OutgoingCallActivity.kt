package com.bnw.voip.ui.outgoingcall

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bnw.voip.R
import com.bnw.voip.domain.usecase.call.HangupCallUseCase
import com.bnw.voip.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OutgoingCallActivity : AppCompatActivity() {

    @Inject
    lateinit var hangupCallUseCase: HangupCallUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_call)

        val phoneNumber = intent.getStringExtra(AppConstants.PHONE_NUMBER)
        findViewById<TextView>(R.id.phoneNumber).text = phoneNumber

        findViewById<android.widget.ImageButton>(R.id.endCallButton).setOnClickListener {
            lifecycleScope.launch {
                hangupCallUseCase()
                finish()
            }
        }
    }
}