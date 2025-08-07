package com.zeynekurtulus.wayfare.presentation.splash

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.zeynekurtulus.wayfare.MainActivity
import com.zeynekurtulus.wayfare.databinding.ActivitySplashBinding
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager

/**
 * SplashActivity - Custom splash screen for Wayfare
 * 
 * This activity serves as the app's entry point and provides:
 * - Beautiful animated logo and branding
 * - App initialization and loading
 * - Automatic navigation to appropriate screen
 * - Smooth user experience during app startup
 * 
 * Features:
 * - Fade-in logo animation
 * - Travel-themed loading message
 * - Progress indicator
 * - Automatic navigation after 3 seconds
 * - Session check for direct navigation
 */
class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    
    companion object {
        private const val SPLASH_DURATION = 3000L // 3 seconds
        private const val ANIMATION_DURATION = 1500L // 1.5 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize binding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SharedPreferences manager
        sharedPreferencesManager = SharedPreferencesManager(this)
        
        // Set up the splash screen
        setupSplashScreen()
        
        // Start animations
        startAnimations()
        
        // Navigate after delay
        navigateAfterDelay()
    }
    
    private fun setupSplashScreen() {
        // Hide system UI for immersive experience
        hideSystemUI()
        
        // Set initial states for animations
        binding.apply {
            logoImageView.alpha = 0f
            logoImageView.scaleX = 0.5f
            logoImageView.scaleY = 0.5f
            
            appNameTextView.alpha = 0f
            taglineTextView.alpha = 0f
            loadingTextView.alpha = 0f
            progressBar.alpha = 0f
        }
    }
    
    private fun startAnimations() {
        // Animate logo entrance
        animateLogo()
        
        // Animate text elements with delays
        animateTextElements()
        
        // Start progress animation
        animateProgress()
    }
    
    private fun animateLogo() {
        // Logo scale and fade in animation
        val scaleX = ObjectAnimator.ofFloat(binding.logoImageView, "scaleX", 0.5f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(binding.logoImageView, "scaleY", 0.5f, 1.0f)
        val alpha = ObjectAnimator.ofFloat(binding.logoImageView, "alpha", 0f, 1f)
        
        scaleX.duration = ANIMATION_DURATION
        scaleY.duration = ANIMATION_DURATION
        alpha.duration = ANIMATION_DURATION
        
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()
        alpha.interpolator = AccelerateDecelerateInterpolator()
        
        scaleX.start()
        scaleY.start()
        alpha.start()
    }
    
    private fun animateTextElements() {
        // App name animation (delayed)
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(binding.appNameTextView, "alpha", 0f, 1f).apply {
                duration = 800
                start()
            }
        }, 500)
        
        // Tagline animation (more delayed)
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(binding.taglineTextView, "alpha", 0f, 1f).apply {
                duration = 800
                start()
            }
        }, 1000)
        
        // Loading text animation (most delayed)
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(binding.loadingTextView, "alpha", 0f, 1f).apply {
                duration = 600
                start()
            }
        }, 1500)
    }
    
    private fun animateProgress() {
        // Progress bar fade in
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(binding.progressBar, "alpha", 0f, 1f).apply {
                duration = 600
                start()
            }
        }, 2000)
    }
    
    private fun navigateAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DURATION)
    }
    
    private fun navigateToNextScreen() {
        // Check if user is logged in to navigate appropriately
        val isLoggedIn = sharedPreferencesManager.isLoggedIn()
        
        val intent = if (isLoggedIn) {
            // Navigate to main app if user is logged in
            Intent(this, MainActivity::class.java)
        } else {
            // Navigate to login/onboarding if user is not logged in
            // For now, navigate to MainActivity (you can change this to LoginActivity later)
            Intent(this, MainActivity::class.java)
        }
        
        startActivity(intent)
        
        // Add smooth transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        
        // Finish splash activity so user can't go back to it
        finish()
    }
    
    private fun hideSystemUI() {
        // Enable immersive mode for a clean splash screen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }
    

}