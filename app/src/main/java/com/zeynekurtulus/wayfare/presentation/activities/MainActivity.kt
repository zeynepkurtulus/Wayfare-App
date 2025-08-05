package com.zeynekurtulus.wayfare.presentation.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ActivityMainBinding
import com.zeynekurtulus.wayfare.databinding.LayoutBottomNavigationBinding
import com.zeynekurtulus.wayfare.presentation.navigation.BottomNavigationHandler

/**
 * MainActivity - Main app container with bottom navigation and fragments
 * 
 * This activity serves as the main hub for the Wayfare travel planning app.
 * It contains:
 * - Fragment container for different screens
 * - Bottom navigation managed by BottomNavigationHandler
 * - Clean separation of concerns
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationHandler: BottomNavigationHandler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!bottomNavigationHandler.handleBackPress()) {
                    finishAffinity()
                }
            }
        })
        
        setupMainActivity()
        setupBottomNavigation()
    }
    
    private fun setupMainActivity() {
        // Remove action bar for full screen experience
        supportActionBar?.hide()
    }
    
    private fun setupBottomNavigation() {
        // Get the included bottom navigation layout binding
        val bottomNavBinding = LayoutBottomNavigationBinding.bind(binding.bottomNavigationInclude.root)
        
        // Initialize bottom navigation handler
        bottomNavigationHandler = BottomNavigationHandler(
            context = this,
            binding = bottomNavBinding,
            fragmentManager = supportFragmentManager,
            fragmentContainerId = R.id.fragmentContainer
        )
    }
    
    /**
     * Public method to switch to Trip Maker tab from other fragments
     */
    fun switchToTripMaker() {
        bottomNavigationHandler.switchToTab(BottomNavigationHandler.NavigationTab.TRIP_MAKER)
    }

}