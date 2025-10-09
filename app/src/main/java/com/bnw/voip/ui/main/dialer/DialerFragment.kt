package com.bnw.voip.ui.main.dialer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bnw.voip.databinding.FragmentDialerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialerFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentDialerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DialerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialerBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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
            viewModel.onDeletePressed()
        }

        // Call button
        binding.btnCall.setOnClickListener {
            viewModel.onCallPressed()
        }

        viewModel.phoneNumber.observe(viewLifecycleOwner) {
            binding.phoneNumber.text = it
        }
    }

    override fun onClick(v: View?) {
        if (v is TextView) {  // now using TextView instead of Button
            // Only append the first line (to avoid adding \nABC etc.)
            val text = v.text.toString().split("\n")[0]
            viewModel.onDigitPressed(text)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
