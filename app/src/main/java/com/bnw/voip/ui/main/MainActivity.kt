package com.bnw.voip.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bnw.voip.R
import com.bnw.voip.databinding.ActivityMainBinding
import com.bnw.voip.domain.usecase.SyncContactsUseCase
import com.bnw.voip.utils.CallService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var syncContactsUseCase: SyncContactsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for modern look
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupServices()
        setupNavigation()
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
    }
    
    private fun syncContacts() {
        lifecycleScope.launch {
            syncContactsUseCase()
        }
    }
}
