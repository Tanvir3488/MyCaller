package com.bnw.voip.ui.main.dialer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
        setupDialpadKeys()
        setupActionButtons()
        observePhoneNumber()
    }

    private fun setupDialpadKeys() {
        // Dialpad number keys
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
    }

    private fun setupActionButtons() {
        // Delete button with enhanced interaction
        binding.btnDelete.setOnClickListener {
            animateButtonPress(it)
            viewModel.onDeletePressed()
        }

        // Call button with enhanced interaction
        binding.btnCall.setOnClickListener {
            animateCallButton(it)
            viewModel.onCallPressed()
        }
    }

    private fun observePhoneNumber() {
        viewModel.phoneNumber.observe(viewLifecycleOwner) { phoneNumber ->
            binding.phoneNumber.text = phoneNumber
            // Show/hide delete button based on whether there's a number
            binding.btnDelete.visibility = if (phoneNumber.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        if (v is TextView) {
            // Add subtle animation to key press
            animateKeyPress(v)
            
            // Extract only the digit (first character) to avoid adding letters
            val text = v.text.toString().split("\n")[0]
            viewModel.onDigitPressed(text)
        }
    }

    private fun animateKeyPress(view: View) {
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f)
            )
            duration = 100
        }
        
        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f)
            )
            duration = 100
        }
        
        scaleDown.setAnimator {
            scaleUp.start()
        }
        scaleDown.start()
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

    private fun animateCallButton(view: View) {
        val pulseAnimation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
            )
            duration = 200
        }
        pulseAnimation.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Extension function for AnimatorSet
private fun AnimatorSet.setAnimator(action: () -> Unit) {
    this.addListener(object : android.animation.AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: android.animation.Animator) {
            action()
        }
    })
}
