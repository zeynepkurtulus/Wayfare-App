package com.zeynekurtulus.wayfare.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentProfileBinding
import com.zeynekurtulus.wayfare.domain.model.User
import com.zeynekurtulus.wayfare.domain.model.UserPreferences
import com.zeynekurtulus.wayfare.presentation.activities.LoginActivity
import com.zeynekurtulus.wayfare.presentation.activities.EditProfileActivity
import com.zeynekurtulus.wayfare.presentation.activities.ChangePasswordActivity
import com.zeynekurtulus.wayfare.presentation.activities.AccountSettingsActivity
import com.zeynekurtulus.wayfare.presentation.viewmodels.UserProfileViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.UserInfoState
import com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteAccountState
import com.zeynekurtulus.wayfare.utils.DialogUtils
import com.zeynekurtulus.wayfare.utils.getAppContainer

/**
 * ProfileFragment - User profile and settings screen
 */
class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var userProfileViewModel: UserProfileViewModel
    
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
        
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        
        Log.d("ProfileFragment", "ProfileFragment initialized successfully")
    }
    
    private fun setupViewModel() {
        userProfileViewModel = ViewModelProvider(
            this,
            requireActivity().getAppContainer().viewModelFactory
        )[UserProfileViewModel::class.java]
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // Edit Personal Info
            editPersonalInfoButton.setOnClickListener {
                Log.d("ProfileFragment", "Edit personal info clicked")
                navigateToEditProfile()
            }
            
            // Edit Preferences
            editPreferencesButton.setOnClickListener {
                Log.d("ProfileFragment", "Edit preferences clicked")
                navigateToEditProfile()
            }
            
            // Change Password
            changePasswordLayout.setOnClickListener {
                Log.d("ProfileFragment", "Change password clicked")
                navigateToChangePassword()
            }
            
            // Account Management
            accountManagementLayout.setOnClickListener {
                Log.d("ProfileFragment", "Account management clicked")
                navigateToAccountSettings()
            }
            
            // Logout
            logoutLayout.setOnClickListener {
                Log.d("ProfileFragment", "Logout clicked")
                showLogoutConfirmation()
            }
        }
    }
    
    private fun observeViewModel() {
        userProfileViewModel.apply {
            user.observe(viewLifecycleOwner) { user ->
                user?.let { 
                    updateUI(it)
                    Log.d("ProfileFragment", "User data updated: ${user.username}")
                }
            }
            
            isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                Log.d("ProfileFragment", "Loading state: $isLoading")
            }
            
            userInfoState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UserInfoState.Updated -> {
                        DialogUtils.showSuccessDialog(
                            requireContext(),
                            "Success",
                            getString(R.string.changes_saved_successfully)
                        )
                        loadCurrentUser() // Refresh user data
                    }
                    is UserInfoState.Error -> {
                        DialogUtils.showErrorDialog(
                            requireContext(),
                            "Error",
                            state.message
                        )
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }
            
            deleteAccountState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is DeleteAccountState.Success -> {
                        DialogUtils.showSuccessDialog(
                            requireContext(),
                            "Account Deleted",
                            getString(R.string.account_deleted_successfully)
                        )
                        // Navigate to login screen
                        navigateToLogin()
                    }
                    is DeleteAccountState.Error -> {
                        DialogUtils.showErrorDialog(
                            requireContext(),
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
            userFullNameTextView.text = "${user.firstName} ${user.lastName}"
            usernameTextView.text = "@${user.username}"
            emailTextView.text = user.email
            
            // Update home city
            homeCityTextView.text = user.homeCity ?: "Not specified"
            
            // Update preferences
            user.preferences?.let { updatePreferencesUI(it) }
                ?: hidePreferencesSection()
        }
    }
    
    private fun updatePreferencesUI(preferences: UserPreferences) {
        binding.apply {
            // Update budget and travel style
            budgetTextView.text = capitalizeFirst(preferences.budget)
            travelStyleTextView.text = capitalizeFirst(preferences.travelStyle)
            
            // Update interests chips
            interestsChipGroup.removeAllViews()
            preferences.interests.forEach { interest ->
                val chip = Chip(requireContext()).apply {
                    text = capitalizeFirst(interest)
                    isClickable = false
                    setChipBackgroundColorResource(R.color.primary_light)
                    setTextColor(requireContext().getColor(R.color.primary))
                }
                interestsChipGroup.addView(chip)
            }
        }
    }
    
    private fun hidePreferencesSection() {
        binding.apply {
            budgetTextView.text = "Not specified"
            travelStyleTextView.text = "Not specified"
            interestsChipGroup.removeAllViews()
            
            val chip = Chip(requireContext()).apply {
                text = "Not specified"
                isClickable = false
                setChipBackgroundColorResource(R.color.input_background)
                setTextColor(requireContext().getColor(R.color.text_secondary))
            }
            interestsChipGroup.addView(chip)
        }
    }
    
    private fun capitalizeFirst(text: String): String {
        return text.lowercase().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
    
    private fun navigateToEditProfile() {
        try {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error navigating to EditProfile: ${e.message}")
            DialogUtils.showErrorDialog(
                requireContext(),
                "Navigation Error",
                "Could not open edit profile screen. This feature may not be implemented yet."
            )
        }
    }
    
    private fun navigateToChangePassword() {
        try {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error navigating to ChangePassword: ${e.message}")
            DialogUtils.showErrorDialog(
                requireContext(),
                "Navigation Error",
                "Could not open change password screen. This feature may not be implemented yet."
            )
        }
    }
    
    private fun navigateToAccountSettings() {
        try {
            val intent = Intent(requireContext(), AccountSettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error navigating to AccountSettings: ${e.message}")
            DialogUtils.showErrorDialog(
                requireContext(),
                "Navigation Error",
                "Could not open account settings screen. This feature may not be implemented yet."
            )
        }
    }
    
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out")
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton("Log Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun performLogout() {
        userProfileViewModel.logout()
        
        DialogUtils.showSuccessDialog(
            requireContext(),
            "Logged Out",
            getString(R.string.logout_successful)
        )
        
        // Navigate to login screen
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to this fragment
        userProfileViewModel.loadCurrentUser()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}