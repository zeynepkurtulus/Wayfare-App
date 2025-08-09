package com.zeynekurtulus.wayfare.presentation.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ActivityEditProfileBinding
import com.zeynekurtulus.wayfare.domain.model.User
import com.zeynekurtulus.wayfare.domain.model.UserPreferences
import com.zeynekurtulus.wayfare.presentation.viewmodels.UserProfileViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.UserInfoState

import com.zeynekurtulus.wayfare.utils.DialogUtils
import com.zeynekurtulus.wayfare.utils.getAppContainer

class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    
    private var currentUser: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        
        Log.d("EditProfileActivity", "EditProfileActivity initialized")
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
            
            saveButton.setOnClickListener {
                saveProfile()
            }
        }
    }
    
    private fun observeViewModel() {
        userProfileViewModel.apply {
            user.observe(this@EditProfileActivity) { user ->
                user?.let { 
                    currentUser = it
                    populateFields(it)
                    Log.d("EditProfileActivity", "User data loaded: ${user.username}")
                }
            }
            
            isLoading.observe(this@EditProfileActivity) { isLoading ->
                binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.saveButton.isEnabled = !isLoading
                binding.saveButton.text = if (isLoading) getString(R.string.saving) else getString(R.string.save_changes)
            }
            
            userInfoState.observe(this@EditProfileActivity) { state ->
                when (state) {
                    is UserInfoState.Updated -> {
                        DialogUtils.showSuccessDialog(
                            this@EditProfileActivity,
                            "Success",
                            getString(R.string.changes_saved_successfully)
                        )
                        // Close activity after successful save
                        finish()
                    }
                    is UserInfoState.Error -> {
                        DialogUtils.showErrorDialog(
                            this@EditProfileActivity,
                            "Error",
                            state.message
                        )
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }
        }
    }
    
    private fun populateFields(user: User) {
        binding.apply {
            // Populate personal info
            firstNameEditText.setText(user.firstName)
            lastNameEditText.setText(user.lastName)
            homeCityEditText.setText(user.homeCity ?: "")
            
            // Populate preferences if available
            user.preferences?.let { preferences ->
                populatePreferences(preferences)
            }
        }
    }
    
    private fun populatePreferences(preferences: UserPreferences) {
        binding.apply {
            // Set budget preference
            when (preferences.budget.lowercase()) {
                "low" -> budgetLowRadio.isChecked = true
                "medium" -> budgetMediumRadio.isChecked = true
                "high" -> budgetHighRadio.isChecked = true
            }
            
            // Set travel style
            when (preferences.travelStyle.lowercase()) {
                "relaxed" -> travelStyleRelaxedRadio.isChecked = true
                "moderate" -> travelStyleModerateRadio.isChecked = true
                "accelerated" -> travelStyleAcceleratedRadio.isChecked = true
            }
            
            // Set interests
                    val interestCheckboxes = mapOf(
            "Museums" to interestMuseumsCheckbox,
            "Major Museums" to interestMajorMuseumsCheckbox,
            "Theme Parks" to interestThemeParksCheckbox,
            "Parks & Nature" to interestParksNatureCheckbox,
            "Zoos & Aquariums" to interestZoosAquariumsCheckbox,
            "Sports & Recreation" to interestSportsRecreationCheckbox,
            "Entertainment" to interestEntertainmentCheckbox,
            "Wellness & Relaxation" to interestWellnessRelaxationCheckbox,
            "Cultural Sites" to interestCulturalSitesCheckbox,
            "Religious Sites" to interestReligiousSitesCheckbox,
            "Shopping & Markets" to interestShoppingMarketsCheckbox,
            "Tours & Activities" to interestToursActivitiesCheckbox,
            "Landmarks & Monuments" to interestLandmarksMonumentsCheckbox,
            "Transportation" to interestTransportationCheckbox,
            "Other" to interestOtherCheckbox
        )
            
            preferences.interests.forEach { interest ->
                interestCheckboxes[interest]?.isChecked = true
            }
        }
    }
    
    private fun saveProfile() {
        val firstName = binding.firstNameEditText.text.toString().trim()
        val lastName = binding.lastNameEditText.text.toString().trim()
        val homeCity = binding.homeCityEditText.text.toString().trim()
        
        // Validate required fields
        if (firstName.isEmpty()) {
            binding.firstNameEditText.error = getString(R.string.error_first_name_required)
            return
        }
        
        if (lastName.isEmpty()) {
            binding.lastNameEditText.error = getString(R.string.error_last_name_required)
            return
        }
        
        if (homeCity.isEmpty()) {
            binding.homeCityEditText.error = "Home city is required"
            return
        }
        
        // Get selected budget
        Log.d("EditProfileActivity", "Budget radio states - Low: ${binding.budgetLowRadio.isChecked}, Medium: ${binding.budgetMediumRadio.isChecked}, High: ${binding.budgetHighRadio.isChecked}")
        val budget = when {
            binding.budgetLowRadio.isChecked -> "low"
            binding.budgetMediumRadio.isChecked -> "medium"
            binding.budgetHighRadio.isChecked -> "high"
            else -> {
                Log.e("EditProfileActivity", "No budget selected!")
                DialogUtils.showErrorDialog(
                    this,
                    "Validation Error",
                    "Please select a budget preference"
                )
                return
            }
        }
        
        // Get selected travel style
        val travelStyle = when {
            binding.travelStyleRelaxedRadio.isChecked -> "relaxed"
            binding.travelStyleModerateRadio.isChecked -> "moderate"
            binding.travelStyleAcceleratedRadio.isChecked -> "accelerated"
            else -> {
                DialogUtils.showErrorDialog(
                    this,
                    "Validation Error",
                    "Please select a travel style"
                )
                return
            }
        }
        
        // Get selected interests
        val interests = mutableListOf<String>()
        val interestCheckboxes = mapOf(
            "Museums" to binding.interestMuseumsCheckbox,
            "Major Museums" to binding.interestMajorMuseumsCheckbox,
            "Theme Parks" to binding.interestThemeParksCheckbox,
            "Parks & Nature" to binding.interestParksNatureCheckbox,
            "Zoos & Aquariums" to binding.interestZoosAquariumsCheckbox,
            "Sports & Recreation" to binding.interestSportsRecreationCheckbox,
            "Entertainment" to binding.interestEntertainmentCheckbox,
            "Wellness & Relaxation" to binding.interestWellnessRelaxationCheckbox,
            "Cultural Sites" to binding.interestCulturalSitesCheckbox,
            "Religious Sites" to binding.interestReligiousSitesCheckbox,
            "Shopping & Markets" to binding.interestShoppingMarketsCheckbox,
            "Tours & Activities" to binding.interestToursActivitiesCheckbox,
            "Landmarks & Monuments" to binding.interestLandmarksMonumentsCheckbox,
            "Transportation" to binding.interestTransportationCheckbox,
            "Other" to binding.interestOtherCheckbox
        )
        
        interestCheckboxes.forEach { (interest, checkbox) ->
            if (checkbox.isChecked) {
                interests.add(interest)
            }
        }
        
        if (interests.isEmpty()) {
            DialogUtils.showErrorDialog(
                this,
                "Validation Error",
                "Please select at least one interest"
            )
            return
        }
        
        // Create preferences object
        val preferences = UserPreferences(
            interests = interests,
            budget = budget,
            travelStyle = travelStyle
        )
        
        // Update user info via ViewModel
        userProfileViewModel.updateUserInfo(preferences, homeCity)
        
        Log.d("EditProfileActivity", "Saving profile with preferences: $preferences, homeCity: $homeCity")
    }
}