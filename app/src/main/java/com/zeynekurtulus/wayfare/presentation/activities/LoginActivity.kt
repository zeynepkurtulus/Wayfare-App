package com.zeynekurtulus.wayfare.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ActivityLoginBinding
import com.zeynekurtulus.wayfare.presentation.viewmodels.LoginViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.LoginState
import com.zeynekurtulus.wayfare.utils.hideKeyboard
import com.zeynekurtulus.wayfare.utils.showToast
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.DialogUtils

/**
 * LoginActivity - User Authentication Screen
 * 
 * This activity provides a beautiful, consistent login interface that matches
 * the splash screen's travel-themed design. Features include:
 * 
 * - Consistent gradient background matching splash screen
 * - Material Design input fields with floating labels
 * - Real-time form validation with user-friendly error messages
 * - Loading states with progress indicators
 * - Smooth animations and transitions
 * - Integration with LoginViewModel for business logic
 * - Navigation to registration and password recovery
 * 
 * Design Philosophy:
 * - Clean, travel-focused branding
 * - Intuitive user experience
 * - Accessibility-friendly
 * - Consistent with app's visual identity
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    
    private val loginViewModel: LoginViewModel by viewModels {
        getAppContainer().viewModelFactory
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
        
        // Set up the login screen
        setupLoginScreen()
        
        // Set up observers
        setupObservers()
        
        // Set up click listeners
        setupClickListeners()
        
        // Set up text watchers for real-time validation
        setupTextWatchers()
    }
    
    private fun setupLoginScreen() {
        // Set initial focus
        binding.emailEditText.requestFocus()
        
        // Check if user is already logged in
        if (loginViewModel.isUserLoggedIn()) {
            navigateToMain()
        }
    }
    
    private fun setupObservers() {
        // Observe login state
        loginViewModel.loginState.observe(this) { state ->
            handleLoginState(state)
        }
        
        // Observe loading state
        loginViewModel.isLoading.observe(this) { isLoading ->
            handleLoadingState(isLoading)
        }
        
        // Observe form validation errors
        loginViewModel.usernameError.observe(this) { error ->
            binding.emailInputLayout.error = error
        }
        
        loginViewModel.passwordError.observe(this) { error ->
            binding.passwordInputLayout.error = error
        }
    }
    
    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        
        // Login button
        binding.loginButton.setOnClickListener {
            handleLoginClick()
        }
        
        // Register link
        binding.registerTextView.setOnClickListener {
            navigateToRegister()
        }
        
        // Forgot password link
        binding.forgotPasswordTextView.setOnClickListener {
            handleForgotPassword()
        }
        
        // Root layout click to hide keyboard
        binding.rootLayout.setOnClickListener {
            hideKeyboard()
        }
    }
    
    private fun setupTextWatchers() {
        // Clear errors when user types
        binding.emailEditText.addTextChangedListener {
            if (binding.emailInputLayout.error != null) {
                binding.emailInputLayout.error = null
            }
        }
        
        binding.passwordEditText.addTextChangedListener {
            if (binding.passwordInputLayout.error != null) {
                binding.passwordInputLayout.error = null
            }
        }
    }
    
    private fun handleLoginClick() {
        // Hide keyboard
        hideKeyboard()
        
        // Get input values
        val username = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        
        // Clear previous errors
        loginViewModel.clearErrors()
        
        // Attempt login
        loginViewModel.login(username, password)
    }
    
    private fun handleLoginState(state: LoginState) {
        when (state) {
            is LoginState.Idle -> {
                // Do nothing, waiting for user input
            }
            is LoginState.Success -> {
                DialogUtils.showAutoSuccessDialog(
                    context = this,
                    title = "Welcome Back! 🎉",
                    message = "You have successfully logged in to Wayfare. Get ready to explore!"
                ) {
                    navigateToMain()
                }
            }
            is LoginState.Error -> {
                DialogUtils.showErrorDialog(
                    context = this,
                    title = getString(R.string.dialog_login_error_title),
                    message = state.message,
                    buttonText = getString(R.string.dialog_button_try_again)
                )
            }
        }
    }
    
    private fun handleLoadingState(isLoading: Boolean) {
        binding.apply {
            // Show/hide progress bar
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            // Disable/enable form elements
            loginButton.isEnabled = !isLoading
            emailEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
            registerTextView.isEnabled = !isLoading
            forgotPasswordTextView.isEnabled = !isLoading
            
            // Update button text
            loginButton.text = if (isLoading) {
                getString(R.string.logging_in)
            } else {
                getString(R.string.sign_in)
            }
        }
    }
    
    private fun navigateToRegister() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
    
    private fun handleForgotPassword() {
        // TODO: Implement forgot password functionality
        DialogUtils.showInfoDialog(
            context = this,
            title = getString(R.string.dialog_info_title),
            message = getString(R.string.dialog_password_recovery_coming_soon),
            buttonText = getString(R.string.dialog_button_ok)
        )
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        
        // Finish login activity so user can't go back
        finish()
    }

}