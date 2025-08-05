package com.zeynekurtulus.wayfare.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ActivitySignUpBinding
import com.zeynekurtulus.wayfare.presentation.viewmodels.SignUpViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.RegistrationState
import com.zeynekurtulus.wayfare.presentation.viewmodels.RegistrationField
import com.zeynekurtulus.wayfare.utils.hideKeyboard
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.DialogUtils
import com.zeynekurtulus.wayfare.utils.BeautifulDialogUtils
import android.os.Handler
import android.os.Looper
import android.app.AlertDialog

/**
 * SignUpActivity - User Registration Screen
 * 
 * This activity provides a comprehensive registration interface with:
 * - Complete form with 6 input fields (First Name, Last Name, Username, Email, Password, Confirm Password)
 * - Real-time form validation with user-friendly error messages
 * - Integration with SignUpViewModel for business logic
 * - Custom dialog feedback system
 * - Smooth animations and transitions
 * - Scrollable design for all screen sizes
 */
class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignUpBinding
    
    private val signUpViewModel: SignUpViewModel by viewModels {
        getAppContainer().viewModelFactory
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewBinding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToLogin()
            }
        })
        // Set up the sign up screen
        setupSignUpScreen()
        
        // Set up observers
        setupObservers()
        
        // Set up click listeners
        setupClickListeners()
        
        // Set up text watchers for real-time validation
        setupTextWatchers()
    }
    
    private fun setupSignUpScreen() {
        // Set initial focus
        binding.firstNameEditText.requestFocus()
    }
    
    private fun setupObservers() {
        // Observe registration state
        signUpViewModel.registrationState.observe(this) { state ->
            handleRegistrationState(state)
        }
        
        // Observe loading state
        signUpViewModel.isLoading.observe(this) { isLoading ->
            handleLoadingState(isLoading)
        }
        
        // Observe form validation errors
        signUpViewModel.firstNameError.observe(this) { error ->
            binding.firstNameInputLayout.error = error
        }
        
        signUpViewModel.lastNameError.observe(this) { error ->
            binding.lastNameInputLayout.error = error
        }
        
        signUpViewModel.usernameError.observe(this) { error ->
            binding.usernameInputLayout.error = error
        }
        
        signUpViewModel.emailError.observe(this) { error ->
            binding.emailInputLayout.error = error
        }
        
        signUpViewModel.passwordError.observe(this) { error ->
            if (error != null) {
                // Show error dialog that disappears after 3 seconds
                showPasswordErrorDialog(error)
            }
            binding.passwordInputLayout.error = error
        }
        
        signUpViewModel.confirmPasswordError.observe(this) { error ->
            binding.confirmPasswordInputLayout.error = error
        }
    }
    
    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        
        // Sign Up button
        binding.signUpButton.setOnClickListener {
            handleSignUpClick()
        }
        
        // Login link (Already have an account?)
        binding.loginTextView.setOnClickListener {
            navigateToLogin()
        }
        
        // Root layout click to hide keyboard
        binding.rootLayout.setOnClickListener {
            hideKeyboard()
        }
    }
    
    private fun setupTextWatchers() {
        // Clear errors when user types and perform real-time validation
        binding.firstNameEditText.addTextChangedListener {
            signUpViewModel.clearFieldError(RegistrationField.FIRST_NAME)
        }
        
        binding.lastNameEditText.addTextChangedListener {
            signUpViewModel.clearFieldError(RegistrationField.LAST_NAME)
        }
        
        binding.usernameEditText.addTextChangedListener {
            signUpViewModel.clearFieldError(RegistrationField.USERNAME)
        }
        
        binding.emailEditText.addTextChangedListener {
            signUpViewModel.clearFieldError(RegistrationField.EMAIL)
        }
        
        binding.passwordEditText.addTextChangedListener {
            signUpViewModel.clearFieldError(RegistrationField.PASSWORD)
        }
        
        binding.confirmPasswordEditText.addTextChangedListener {
            signUpViewModel.clearFieldError(RegistrationField.CONFIRM_PASSWORD)
        }
    }
    
    private fun handleSignUpClick() {
        // Hide keyboard
        hideKeyboard()
        
        // Get input values
        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()
        
        // Clear previous errors
        signUpViewModel.clearErrors()
        
        // Attempt registration
        signUpViewModel.registerUser(
            username = username,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            firstName = firstName,
            lastName = lastName
        )
    }
    
    private fun handleRegistrationState(state: RegistrationState) {
        when (state) {
            is RegistrationState.Idle -> {
                // Do nothing, waiting for user input
            }
            is RegistrationState.Success -> {
                // Navigate to OTP verification instead of login
                val email = binding.emailEditText.text.toString().trim()
                navigateToOtpVerification(email)
            }
            is RegistrationState.Error -> {
                BeautifulDialogUtils.showRegistrationErrorDialog(
                    context = this,
                    errorMessage = state.message
                ) {
                    // Retry action - clear form errors and let user try again
                    clearFormErrors()
                }
            }
        }
    }
    
    private fun handleLoadingState(isLoading: Boolean) {
        binding.apply {
            // Show/hide progress bar
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            // Disable/enable form elements
            signUpButton.isEnabled = !isLoading
            firstNameEditText.isEnabled = !isLoading
            lastNameEditText.isEnabled = !isLoading
            usernameEditText.isEnabled = !isLoading
            emailEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
            confirmPasswordEditText.isEnabled = !isLoading
            loginTextView.isEnabled = !isLoading
            
            // Update button text
            signUpButton.text = if (isLoading) {
                getString(R.string.creating_account)
            } else {
                getString(R.string.sign_up)
            }
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        
        // Finish sign up activity so user can't go back
        finish()
    }
    
    private fun navigateToOtpVerification(email: String) {
        // Get the registration data from the ViewModel
        val userRegistration = signUpViewModel.getPendingUserRegistration()
        
        val intent = OtpVerificationActivity.createIntent(this, email, userRegistration)
        startActivity(intent)
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        
        // Finish sign up activity so user can't go back
        finish()
    }
    
    private fun showPasswordErrorDialog(errorMessage: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Password Requirements")
            .setMessage(errorMessage)
            .setIcon(R.drawable.ic_error)
            .setCancelable(true)
            .create()
        
        dialog.show()
        
        // Auto-dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
    }
    
    private fun clearFormErrors() {
        binding.apply {
            firstNameInputLayout.error = null
            lastNameInputLayout.error = null
            usernameInputLayout.error = null
            emailInputLayout.error = null
            passwordInputLayout.error = null
            confirmPasswordInputLayout.error = null
        }
    }

}