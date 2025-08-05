package com.zeynekurtulus.wayfare.presentation.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ActivityChangePasswordBinding
import com.zeynekurtulus.wayfare.presentation.viewmodels.UserProfileViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.PasswordChangeState

import com.zeynekurtulus.wayfare.utils.DialogUtils
import com.zeynekurtulus.wayfare.utils.getAppContainer

class ChangePasswordActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        
        Log.d("ChangePasswordActivity", "ChangePasswordActivity initialized")
    }
    
    private fun setupViewModel() {
        userProfileViewModel = ViewModelProvider(
            this,
            getAppContainer().viewModelFactory
        )[UserProfileViewModel::class.java]
    }
    
    private fun setupClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }
            
            changePasswordButton.setOnClickListener {
                changePassword()
            }
        }
    }
    
    private fun observeViewModel() {
        userProfileViewModel.apply {
            isLoading.observe(this@ChangePasswordActivity) { isLoading ->
                binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.changePasswordButton.isEnabled = !isLoading
                binding.changePasswordButton.text = if (isLoading) "Changing Password..." else getString(R.string.change_password)
                
                // Disable input fields while loading
                binding.currentPasswordEditText.isEnabled = !isLoading
                binding.newPasswordEditText.isEnabled = !isLoading
                binding.confirmPasswordEditText.isEnabled = !isLoading
            }
            
            passwordChangeState.observe(this@ChangePasswordActivity) { state ->
                when (state) {
                    is PasswordChangeState.Success -> {
                        showBeautifulPasswordSuccessDialog()
                    }
                    is PasswordChangeState.Error -> {
                        DialogUtils.showErrorDialog(
                            this@ChangePasswordActivity,
                            "Error",
                            state.message
                        )
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }
        }
    }
    
    private fun changePassword() {
        clearErrors()
        
        val currentPassword = binding.currentPasswordEditText.text.toString()
        val newPassword = binding.newPasswordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()
        
        // Validate inputs
        var hasError = false
        
        if (currentPassword.isEmpty()) {
            binding.currentPasswordLayout.error = getString(R.string.error_current_password_required)
            hasError = true
        }
        
        if (newPassword.isEmpty()) {
            binding.newPasswordLayout.error = getString(R.string.error_new_password_required)
            hasError = true
        } else if (newPassword.length < 8) {
            binding.newPasswordLayout.error = getString(R.string.error_new_password_too_short)
            hasError = true
        }
        
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordLayout.error = getString(R.string.error_confirm_new_password_required)
            hasError = true
        } else if (newPassword != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.error_new_passwords_dont_match)
            hasError = true
        }
        
        // Check if new password is different from current password
        if (currentPassword == newPassword) {
            binding.newPasswordLayout.error = "New password must be different from current password"
            hasError = true
        }
        
        if (hasError) {
            return
        }
        
        // Call ViewModel to change password
        userProfileViewModel.changePassword(currentPassword, newPassword, confirmPassword)
        
        Log.d("ChangePasswordActivity", "Attempting to change password")
    }
    
    private fun clearErrors() {
        binding.apply {
            currentPasswordLayout.error = null
            newPasswordLayout.error = null
            confirmPasswordLayout.error = null
        }
    }
    
    private fun showBeautifulPasswordSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_beautiful_password_success, null)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        
        // Auto-dismiss after 3 seconds and close activity
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 3000)
    }
}