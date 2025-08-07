package com.zeynekurtulus.wayfare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zeynekurtulus.wayfare.databinding.ActivityMainBinding

/**
 * MainActivity - Main entry point after splash screen
 * 
 * This activity serves as the main hub for the Wayfare travel planning app.
 * It will contain the primary navigation and main features.
 * 
 * Note: This activity is reached after the SplashActivity completes.
 * In a complete implementation, this would contain:
 * - Bottom navigation for main app sections
 * - Fragment container for different screens
 * - Toolbar/ActionBar setup
 * - Main app functionality
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up the main activity
        setupMainActivity()
    }
    
    private fun setupMainActivity() {
        // Set up main activity
        supportActionBar?.title = getString(R.string.app_name)
        
        // TODO: In full implementation, this would contain:
        // - Navigation setup (Bottom Navigation, Navigation Component)
        // - Fragment management
        // - User authentication checks
        // - Main app features and screens
    }
}