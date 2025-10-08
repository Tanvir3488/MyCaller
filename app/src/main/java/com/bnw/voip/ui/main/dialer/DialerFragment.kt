package com.bnw.voip.ui.main.dialer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bnw.voip.MyApplication.Companion.sipManager
import com.bnw.voip.databinding.FragmentDialerBinding

class DialerFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentDialerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dialpad keys
        binding.button0.setOnClickListener(this)
        binding.button1.setOnClickListener(this)
        binding.button2.setOnClickListener(this)
        binding.button3.setOnClickListener(this)
        binding.button4.setOnClickListener(this)
        binding.button5.setOnClickListener(this)
        binding.button6.setOnClickListener(this)
        binding.button7.setOnClickListener(this)
        binding.button8.setOnClickListener(this)
        binding.button9.setOnClickListener(this)
        binding.buttonStar.setOnClickListener(this)
        binding.buttonHash.setOnClickListener(this)

        // Delete button
        binding.btnDelete.setOnClickListener {
            val currentText = binding.phoneNumber.text.toString()
            if (currentText.isNotEmpty()) {
                binding.phoneNumber.text = currentText.dropLast(1)
            }
        }

        // Call button
        binding.btnCall.setOnClickListener {
            val number = binding.phoneNumber.text.toString()
            if (number.isNotEmpty()) {
               sipManager.call(number)
            }
        }
    }

    override fun onClick(v: View?) {
        if (v is TextView) {  // now using TextView instead of Button
            // Only append the first line (to avoid adding \nABC etc.)
            val text = v.text.toString().split("\n")[0]
            binding.phoneNumber.append(text)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
