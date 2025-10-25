package com.bnw.voip.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bnw.voip.R
import com.bnw.voip.data.datastore.UserManager
import com.bnw.voip.databinding.ActivityMainBinding
import com.bnw.voip.domain.usecase.SyncContactsUseCase
import com.bnw.voip.ui.login.LoginActivity
import com.bnw.voip.utils.CallService
import com.bnw.voip.voip.CustomeSipManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var syncContactsUseCase: SyncContactsUseCase
    
    @Inject
    lateinit var userManager: UserManager
    
    @Inject
    lateinit var sipManager: CustomeSipManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for modern look
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupServices()
        setupNavigation()
        setupLogoutButton()
        syncContacts()
    }
    
    private fun setupServices() {
        val serviceIntent = Intent(this, CallService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    
    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavView.setupWithNavController(navController)
        
        // Listen for navigation changes to update toolbar title
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val title = when (destination.id) {
                R.id.dialerFragment -> "Dialer"
                R.id.callHistoryFragment -> "Call History"
                R.id.contactsFragment -> "Contacts"
                else -> "VoIP App"
            }
            binding.toolbarTitle.text = title
        }
    }
    
    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            sipManager.logout()
            userManager.clearUser()
            
            // Navigate to login activity
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun syncContacts() {
        lifecycleScope.launch {
            syncContactsUseCase()
        }
    }
}
