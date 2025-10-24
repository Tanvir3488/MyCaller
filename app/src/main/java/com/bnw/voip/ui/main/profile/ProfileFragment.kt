package com.bnw.voip.ui.main.profile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.bnw.voip.R
import com.bnw.voip.databinding.FragmentProfileBinding
import com.bnw.voip.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUserProfile()
        setupClickListeners()
    }

    private fun setupUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                if (user != null) {
                    binding.tvName.text = user.name
                    binding.tvUserInfo.text = "Active" // You can change this to show email or status
                    
                    // Load profile image with modern styling
                    binding.ivProfile.load(user.profilePictureUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_profile)
                        error(R.drawable.ic_profile)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    // Show default values when no user is logged in
                    binding.tvName.text = "Guest User"
                    binding.tvUserInfo.text = "Not logged in"
                    binding.ivProfile.setImageResource(R.drawable.ic_profile)
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Edit profile button
        binding.editProfileButton.setOnClickListener {
            animateButtonPress(it)
            // TODO: Navigate to edit profile screen
            Toast.makeText(requireContext(), "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        
        // Profile settings item
        binding.profileSettingsItem.setOnClickListener {
            animateItemPress(it)
            // TODO: Navigate to settings screen
            Toast.makeText(requireContext(), "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        
        // App info item
        binding.appInfoItem.setOnClickListener {
            animateItemPress(it)
            // TODO: Navigate to about screen
            Toast.makeText(requireContext(), "About - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        // Logout button with confirmation
        binding.btnLogout.setOnClickListener {
            animateLogoutButton(it)
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        viewModel.logout()
        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun animateButtonPress(view: View) {
        val scaleAnimation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f)
            )
            duration = 150
        }
        scaleAnimation.start()
    }

    private fun animateItemPress(view: View) {
        val scaleAnimation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.98f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.98f, 1f)
            )
            duration = 100
        }
        scaleAnimation.start()
    }

    private fun animateLogoutButton(view: View) {
        val pulseAnimation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f)
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
