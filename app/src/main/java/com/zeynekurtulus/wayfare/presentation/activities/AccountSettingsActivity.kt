package com.zeynekurtulus.wayfare.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ActivityAccountSettingsBinding
import com.zeynekurtulus.wayfare.domain.model.User
import com.zeynekurtulus.wayfare.presentation.activities.LoginActivity
import com.zeynekurtulus.wayfare.presentation.viewmodels.UserProfileViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteAccountState

import com.zeynekurtulus.wayfare.utils.DialogUtils
import com.zeynekurtulus.wayfare.utils.getAppContainer
import java.text.SimpleDateFormat
import java.util.*

class AccountSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    
    private var currentUser: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        
        Log.d("AccountSettingsActivity", "AccountSettingsActivity initialized")
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
            
            deleteAccountButton.setOnClickListener {
                showDeleteAccountConfirmation()
            }
        }
    }
    
    private fun observeViewModel() {
        userProfileViewModel.apply {
            user.observe(this@AccountSettingsActivity) { user ->
                user?.let { 
                    currentUser = it
                    updateUI(it)
                    Log.d("AccountSettingsActivity", "User data loaded: ${user.username}")
                }
            }
            
            isLoading.observe(this@AccountSettingsActivity) { isLoading ->
                binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.deleteAccountButton.isEnabled = !isLoading
            }
            
            deleteAccountState.observe(this@AccountSettingsActivity) { state ->
                when (state) {
                    is DeleteAccountState.Success -> {
                        DialogUtils.showSuccessDialog(
                            this@AccountSettingsActivity,
                            "Account Deleted",
                            getString(R.string.account_deleted_successfully)
                        )
                        // Navigate to login screen and clear all activities
                        navigateToLogin()
                    }
                    is DeleteAccountState.Error -> {
                        DialogUtils.showErrorDialog(
                            this@AccountSettingsActivity,
                            "Error",
                            state.message
                        )
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }
        }
    }
    
    private fun updateUI(user: User) {
        binding.apply {
            // Update user info
            usernameTextView.text = "@${user.username}"
            emailTextView.text = user.email
            
            // Mock data for demonstration - in real app this would come from user data
            tripsCountTextView.text = "Loading..."
            memberSinceTextView.text = "Loading..."
            
            // You could add API calls to get user statistics here
            // For now, showing placeholder data
            updateAccountStats()
        }
    }
    
    private fun updateAccountStats() {
        // Mock data for demonstration
        binding.apply {
            tripsCountTextView.text = "5 trips"
            
            // Format current date as member since
            val currentDate = Date()
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            memberSinceTextView.text = formatter.format(currentDate)
        }
    }
    
    private fun showDeleteAccountConfirmation() {
        // Create a custom dialog layout for password input
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        val passwordLayout = dialogView.findViewById<TextInputLayout>(R.id.passwordLayout)
        val passwordEditText = dialogView.findViewById<TextInputEditText>(R.id.passwordEditText)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account")
            .setMessage(getString(R.string.delete_account_confirmation))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                val password = passwordEditText.text.toString()
                if (password.isEmpty()) {
                    DialogUtils.showErrorDialog(
                        this,
                        "Validation Error",
                        "Password is required to delete account"
                    )
                    return@setPositiveButton
                }
                
                // Proceed with account deletion
                userProfileViewModel.deleteAccount(password)
                Log.d("AccountSettingsActivity", "Attempting to delete account")
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}