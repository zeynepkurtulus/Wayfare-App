package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.chip.Chip
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentTripMakerBinding
import com.zeynekurtulus.wayfare.domain.model.City
import com.zeynekurtulus.wayfare.domain.model.RouteDetail
import com.zeynekurtulus.wayfare.domain.model.RouteDay
import com.zeynekurtulus.wayfare.domain.model.Activity
import com.zeynekurtulus.wayfare.presentation.adapters.CitySuggestionsAdapter
import com.zeynekurtulus.wayfare.presentation.adapters.MustVisitPlacesAdapter
import com.zeynekurtulus.wayfare.presentation.adapters.SelectedPlacesAdapter
import com.zeynekurtulus.wayfare.presentation.viewmodels.TripMakerViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteCreationResult
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.PrivacyToggleState
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.bumptech.glide.Glide

/**
 * TripMakerFragment - Fragment for creating and planning trips
 * 
 * This fragment handles the step-by-step trip creation flow:
 * 1. Welcome screen
 * 2. Destination selection
 * 3. Date selection
 * 4. Category selection
 * 5. Season selection
 * 6. Loading screen
 * 7. Results screen
 */
class TripMakerFragment : Fragment() {

    private var _binding: FragmentTripMakerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TripMakerViewModel by viewModels {
        getAppContainer().viewModelFactory
    }
    
    // ⭐ NEW: RouteListViewModel for privacy functionality
    private val routeListViewModel: RouteListViewModel by viewModels {
        getAppContainer().viewModelFactory
    }

    private lateinit var citySuggestionsAdapter: CitySuggestionsAdapter
    private lateinit var mustVisitPlacesAdapter: MustVisitPlacesAdapter
    private lateinit var selectedPlacesAdapter: SelectedPlacesAdapter
    

    private var currentPrivacySwitch: com.google.android.material.materialswitch.MaterialSwitch? = null
    private var currentPrivacyIcon: ImageView? = null
    private var currentPrivacyDescription: TextView? = null
    private var currentRouteId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripMakerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize city suggestions adapter first
        initializeCitySuggestionsAdapter()
        setupObservers()
        setupStepFlow()
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("TripMakerFragment", "onResume: Trip maker fragment resumed")
        
        // Check if user is returning from another screen and reset if needed
        handleNavigationReturn()
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("TripMakerFragment", "onPause: Trip maker fragment paused")
        
        // Check if user has unsaved changes and show warning if needed
        if (viewModel.hasUnsavedChanges() && !isNavigatingBack) {
            showUnsavedChangesWarning()
        } else {
            // Mark that user navigated away (for potential reset on return)
            markNavigatedAway()
        }
    }
    
    private var isNavigatingBack = false
    
    private fun showUnsavedChangesWarning() {
        if (!isAdded || activity == null) return
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Unsaved Changes")
        builder.setMessage("You have unsaved changes in your trip creation. If you leave now, your progress will be lost.\n\nAre you sure you want to continue?")
        
        // Create custom view for better styling
        val dialogView = layoutInflater.inflate(R.layout.dialog_unsaved_changes, null)
        builder.setView(dialogView)
        
        val dialog = builder.create()
        
        // Find buttons in custom layout
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.cancelButton)
        val continueButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.continueButton)
        
        cancelButton.setOnClickListener {
            Log.d("TripMakerFragment", "User chose to stay and continue editing")
            dialog.dismiss()
            // User chose to stay, no further action needed
        }
        
        continueButton.setOnClickListener {
            Log.d("TripMakerFragment", "User chose to discard changes and leave")
            dialog.dismiss()
            isNavigatingBack = true
            markNavigatedAway()
            // Reset the trip maker since user confirmed leaving
            viewModel.resetFlow()
        }
        
        // Make dialog background white and dim the background
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        dialog.window?.setDimAmount(0.6f) // Dim the background
        
        dialog.show()
    }
    
    private fun handleNavigationReturn() {
        // Reset trip maker to welcome screen when user returns
        // This provides a clean slate for new trip creation
        if (shouldResetOnReturn()) {
            Log.d("TripMakerFragment", "Resetting trip maker to welcome screen")
            resetTripMakerState()
        }
    }
    
    private fun shouldResetOnReturn(): Boolean {
        // Always reset for clean UX - users expect fresh start when returning to trip maker
        return true
    }
    
    private fun markNavigatedAway() {
        // We could store this in SharedPreferences or ViewModel if needed
        // For now, we'll use the simple approach of always resetting on return
    }
    
    private fun resetTripMakerState() {
        // Reset ViewModel state
        viewModel.resetFlow()
        
        // Clear any UI state that might persist
        clearUIState()
        
        // Ensure we start from the welcome screen
        updateStepDisplay(0)
    }
    
    private fun clearUIState() {
        // Clear city search field
        val searchEditText = binding.destinationStep.root.findViewById<TextInputEditText>(R.id.destinationSearchEditText)
        searchEditText?.setText("")
        
        // Hide city suggestions
        val suggestionsRecyclerView = binding.destinationStep.root.findViewById<RecyclerView>(R.id.citySuggestionsRecyclerView)
        suggestionsRecyclerView?.visibility = View.GONE
        
        // Clear selected city card
        val selectedCityCard = binding.destinationStep.root.findViewById<View>(R.id.selectedCityCard)
        selectedCityCard?.visibility = View.GONE
        
        // Clear any other persistent UI state
        clearPrivacyReferences()
        
        Log.d("TripMakerFragment", "UI state cleared")
    }
    
    private fun clearPrivacyReferences() {
        // Clear privacy control references to avoid stale state
        currentPrivacySwitch = null
        currentPrivacyIcon = null
        currentPrivacyDescription = null
        currentRouteId = null
    }

    private fun setupObservers() {
        viewModel.currentStep.observe(viewLifecycleOwner, Observer { step ->
            updateStepDisplay(step)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // Handle loading state if needed
        })

        viewModel.routeCreationResult.observe(viewLifecycleOwner, Observer { result ->
            handleRouteCreationResult(result)
        })

        // City search observers
        viewModel.citySearchResults.observe(viewLifecycleOwner, Observer { cities ->
            android.util.Log.d("TripMakerFragment", "Received ${cities.size} cities in observer")
            cities.forEachIndexed { index, city ->
                android.util.Log.d("TripMakerFragment", "City $index: ${city.displayText}")
            }
            
            if (::citySuggestionsAdapter.isInitialized) {
                citySuggestionsAdapter.updateCities(cities)
                val suggestionsRecyclerView = binding.destinationStep.root.findViewById<RecyclerView>(R.id.citySuggestionsRecyclerView)
                android.util.Log.d("TripMakerFragment", "RecyclerView found: ${suggestionsRecyclerView != null}")
                
                if (cities.isNotEmpty()) {
                    android.util.Log.d("TripMakerFragment", "Showing RecyclerView with ${cities.size} cities")
                    suggestionsRecyclerView?.visibility = View.VISIBLE
                    android.util.Log.d("TripMakerFragment", "RecyclerView visibility set to VISIBLE")
                } else {
                    android.util.Log.d("TripMakerFragment", "Hiding RecyclerView - no cities")
                    suggestionsRecyclerView?.visibility = View.GONE
                }
            } else {
                android.util.Log.e("TripMakerFragment", "Adapter not initialized when trying to update cities")
            }
        })

        viewModel.isSearching.observe(viewLifecycleOwner, Observer { isSearching ->
            val loadingProgressBar = binding.destinationStep.root.findViewById<View>(R.id.searchLoadingProgressBar)
            loadingProgressBar?.visibility = if (isSearching) View.VISIBLE else View.GONE
        })

        viewModel.tripData.observe(viewLifecycleOwner, Observer { tripData ->
            tripData.selectedCity?.let { selectedCity ->
                displaySelectedCity(selectedCity)
            }
        })
        
        // ⭐ NEW: Observe privacy toggle state
        routeListViewModel.privacyToggleState.observe(viewLifecycleOwner, Observer { state ->
            handlePrivacyToggleState(state)
        })
    }
    
    // ⭐ NEW: Handle privacy toggle state changes
    private fun handlePrivacyToggleState(state: PrivacyToggleState) {
        when (state) {
            is PrivacyToggleState.Loading -> {
                // Disable privacy switch during loading
                currentPrivacySwitch?.isEnabled = false
                Log.d("TripMakerFragment", "Privacy toggle in progress...")
            }
            is PrivacyToggleState.Success -> {
                // Re-enable switch and update UI
                currentPrivacySwitch?.isEnabled = true
                updateCurrentPrivacyUI(state.isPublic)
                Log.d("TripMakerFragment", "Privacy toggle successful: ${state.isPublic}")
                
                // Clear the state to avoid re-processing
                routeListViewModel.clearPrivacyToggleState()
            }
            is PrivacyToggleState.Error -> {
                // Re-enable switch and revert state
                currentPrivacySwitch?.isEnabled = true
                revertPrivacySwitch()
                Log.e("TripMakerFragment", "Privacy toggle failed: ${state.message}")
                
                // Show error message to user
                android.widget.Toast.makeText(
                    requireContext(), 
                    "Failed to update privacy setting. Please try again.", 
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                // Clear the state to avoid re-processing
                routeListViewModel.clearPrivacyToggleState()
            }
            is PrivacyToggleState.Idle -> {
                // Do nothing
            }
        }
    }
    
    // ⭐ NEW: Helper functions for privacy UI management
    private fun updateCurrentPrivacyUI(isPublic: Boolean) {
        currentPrivacyIcon?.let { icon ->
            currentPrivacyDescription?.let { description ->
                currentPrivacySwitch?.let { switch ->
                    updatePrivacyUI(isPublic, icon, description, switch)
                }
            }
        }
    }
    
    private fun revertPrivacySwitch() {
        currentPrivacySwitch?.let { switch ->
            // Temporarily remove listener to avoid infinite loops
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = !switch.isChecked
            // Re-attach listener
            setupPrivacySwitchListener()
        }
    }
    
    private fun setupPrivacySwitchListener() {
        currentPrivacySwitch?.setOnCheckedChangeListener { _, isChecked ->
            currentRouteId?.let { routeId ->
                Log.d("TripMakerFragment", "Privacy toggle changed to: $isChecked for route: $routeId")
                routeListViewModel.toggleRoutePrivacy(routeId, isChecked)
            }
        }
    }

    private fun setupStepFlow() {
        updateStepDisplay(0)
    }

    private fun updateStepDisplay(step: Int) {
        // Update step counter
        val stepText = binding.stepText
        stepText.text = "Step ${step + 1} of ${viewModel.totalSteps}"
        
        // Update progress bar (calculate progress as percentage)
        val progressBar = binding.progressIndicator
        val progressPercentage = ((step + 1).toFloat() / viewModel.totalSteps.toFloat() * 100).toInt()
        progressBar.progress = progressPercentage
        
        Log.d("TripMakerFragment", "Step ${step + 1}/${viewModel.totalSteps} - Progress: $progressPercentage%")
        
        binding.apply {
            welcomeStep.root.visibility = View.GONE
            destinationStep.root.visibility = View.GONE
            dateStep.root.visibility = View.GONE
            categoryStep.root.visibility = View.GONE
            seasonStep.root.visibility = View.GONE
            mustVisitStep.root.visibility = View.GONE
            interestsStep.root.visibility = View.GONE
            budgetStep.root.visibility = View.GONE
            travelStyleStep.root.visibility = View.GONE
            titleStep.root.visibility = View.GONE
            loadingStep.root.visibility = View.GONE
            resultsStep.root.visibility = View.GONE
            tripDetailsStep.root.visibility = View.GONE
        }

        when (step) {
            0 -> {
                binding.welcomeStep.root.visibility = View.VISIBLE
                setupWelcomeStep()
            }
            1 -> {
                binding.destinationStep.root.visibility = View.VISIBLE
                setupDestinationStep()
            }
            2 -> {
                binding.dateStep.root.visibility = View.VISIBLE
                setupDateStep()
            }
            3 -> {
                binding.categoryStep.root.visibility = View.VISIBLE
                setupCategoryStep()
            }
            4 -> {
                binding.seasonStep.root.visibility = View.VISIBLE
                setupSeasonStep()
            }
            5 -> {
                binding.interestsStep.root.visibility = View.VISIBLE
                setupInterestsStep()
            }
            6 -> {
                binding.budgetStep.root.visibility = View.VISIBLE
                setupBudgetStep()
            }
            7 -> {
                binding.travelStyleStep.root.visibility = View.VISIBLE
                setupTravelStyleStep()
            }
            8 -> {
                binding.mustVisitStep.root.visibility = View.VISIBLE
                setupMustVisitStep()
            }
            9 -> {
                binding.titleStep.root.visibility = View.VISIBLE
                setupTitleStep()
            }
            10 -> {
                binding.loadingStep.root.visibility = View.VISIBLE
                setupLoadingStep()
            }
            11 -> {
                binding.resultsStep.root.visibility = View.VISIBLE
                setupResultsStep()
            }
            // Note: Step 12 (trip details) is not part of the normal flow
            // It's only accessible via the "View Detailed Itinerary" button
        }
    }

    private fun setupWelcomeStep() {
        // Set up welcome step UI and click listeners
        val startButton = binding.welcomeStep.root.findViewById<Button>(R.id.welcomeNextButton)
        startButton?.setOnClickListener {
            viewModel.nextStep()
        }
    }

    private fun initializeCitySuggestionsAdapter() {
        // Initialize city suggestions adapter early
        citySuggestionsAdapter = CitySuggestionsAdapter { selectedCity ->
            onCitySelected(selectedCity)
        }
    }

    private fun setupDestinationStep() {
        // Set up city search functionality
        val searchEditText = binding.destinationStep.root.findViewById<TextInputEditText>(R.id.destinationSearchEditText)
        val suggestionsRecyclerView = binding.destinationStep.root.findViewById<RecyclerView>(R.id.citySuggestionsRecyclerView)
        val nextButton = binding.destinationStep.root.findViewById<Button>(R.id.destinationNextButton)
        val backButton = binding.destinationStep.root.findViewById<Button>(R.id.destinationBackButton)
        
        // Set up RecyclerView
        suggestionsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = citySuggestionsAdapter
        }
        
        // Set up search functionality
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    viewModel.searchCities(query)
                } else {
                    viewModel.clearCitySearch()
                    suggestionsRecyclerView?.visibility = View.GONE
                }
            }
        })
        
        // Button click listeners
        nextButton?.setOnClickListener {
            if (viewModel.canProceedFromDestination()) {
                viewModel.nextStep()
            }
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
    }

    private fun onCitySelected(city: City) {
        val searchEditText = binding.destinationStep.root.findViewById<TextInputEditText>(R.id.destinationSearchEditText)
        val suggestionsRecyclerView = binding.destinationStep.root.findViewById<RecyclerView>(R.id.citySuggestionsRecyclerView)
        val loadingProgressBar = binding.destinationStep.root.findViewById<View>(R.id.searchLoadingProgressBar)
        
        // Set the selected city in ViewModel
        viewModel.setSelectedCity(city)
        
        // Update UI
        searchEditText?.setText(city.displayText)
        suggestionsRecyclerView?.visibility = View.GONE
        loadingProgressBar?.visibility = View.GONE // Stop the loading spinner
        
        // Clear search results and stop any ongoing search
        viewModel.clearCitySearch()
        
        android.util.Log.d("TripMakerFragment", "City selected, stopping loading spinner")
    }

    private fun displaySelectedCity(city: City) {
        val selectedCityCard = binding.destinationStep.root.findViewById<View>(R.id.selectedCityCard)
        val selectedCityTextView = binding.destinationStep.root.findViewById<TextView>(R.id.selectedCityTextView)
        val clearSelectionButton = binding.destinationStep.root.findViewById<ImageButton>(R.id.clearSelectionButton)
        
        android.util.Log.d("TripMakerFragment", "Displaying selected city: ${city.displayText}")
        
        // Update selected city card visibility and content
        selectedCityCard?.visibility = View.VISIBLE
        selectedCityTextView?.text = city.displayText
        
        // Set up clear button
        clearSelectionButton?.setOnClickListener {
            clearSelectedCity()
        }
    }
    
    private fun clearSelectedCity() {
        val selectedCityCard = binding.destinationStep.root.findViewById<View>(R.id.selectedCityCard)
        val searchEditText = binding.destinationStep.root.findViewById<TextInputEditText>(R.id.destinationSearchEditText)
        
        // Hide card and clear search
        selectedCityCard?.visibility = View.GONE
        searchEditText?.setText("")
        
        // Clear from ViewModel (we need to add this method)
        // viewModel.clearSelectedCity()
        
        android.util.Log.d("TripMakerFragment", "Cleared selected city")
    }

    private fun setupDateStep() {
        // Set up date selection UI elements
        val startDateButton = binding.dateStep.root.findViewById<Button>(R.id.selectStartDateButton)
        val endDateButton = binding.dateStep.root.findViewById<Button>(R.id.selectEndDateButton)
        val startDateTextView = binding.dateStep.root.findViewById<TextView>(R.id.selectedStartDateTextView)
        val endDateTextView = binding.dateStep.root.findViewById<TextView>(R.id.selectedEndDateTextView)
        val durationCard = binding.dateStep.root.findViewById<View>(R.id.tripDurationCard)
        val durationTextView = binding.dateStep.root.findViewById<TextView>(R.id.tripDurationTextView)
        
        val nextButton = binding.dateStep.root.findViewById<Button>(R.id.dateNextButton)
        val backButton = binding.dateStep.root.findViewById<Button>(R.id.dateBackButton)
        
        // Date format for display (ensure UTC timezone to match MaterialDatePicker)
        val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        // Start date picker
        startDateButton?.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select start date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            
            datePicker.show(parentFragmentManager, "START_DATE_PICKER")
            
            datePicker.addOnPositiveButtonClickListener { selection ->
                // MaterialDatePicker returns UTC milliseconds, we need to handle timezone properly
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                
                val displayDate = displayDateFormat.format(calendar.time)
                val apiDate = apiDateFormat.format(calendar.time)
                
                startDateTextView?.text = displayDate
                android.util.Log.d("TripMakerFragment", "Start date selected: $apiDate (UTC millis: $selection)")
                
                // Update ViewModel with dates if both are selected
                updateDatesInViewModel(startDateTextView, endDateTextView, durationCard, durationTextView, apiDateFormat)
            }
        }
        
        // End date picker
        endDateButton?.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select end date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            
            datePicker.show(parentFragmentManager, "END_DATE_PICKER")
            
            datePicker.addOnPositiveButtonClickListener { selection ->
                // MaterialDatePicker returns UTC milliseconds, we need to handle timezone properly
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                
                val displayDate = displayDateFormat.format(calendar.time)
                val apiDate = apiDateFormat.format(calendar.time)
                
                endDateTextView?.text = displayDate
                android.util.Log.d("TripMakerFragment", "End date selected: $apiDate (UTC millis: $selection)")
                
                // Update ViewModel with dates if both are selected
                updateDatesInViewModel(startDateTextView, endDateTextView, durationCard, durationTextView, apiDateFormat)
            }
        }
        
        // Navigation buttons
        nextButton?.setOnClickListener {
            if (viewModel.canProceedFromDates()) {
                viewModel.nextStep()
            }
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
    }
    
    private fun updateDatesInViewModel(
        startDateTextView: TextView?,
        endDateTextView: TextView?,
        durationCard: View?,
        durationTextView: TextView?,
        apiDateFormat: SimpleDateFormat
    ) {
        val startDateText = startDateTextView?.text?.toString()
        val endDateText = endDateTextView?.text?.toString()
        
        // Check if both dates are selected (not default text)
        if (startDateText != null && endDateText != null && 
            startDateText != "Select start date" && endDateText != "Select end date") {
            
            try {
                // Parse display dates back to Date objects for duration calculation
                val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val startDate = displayDateFormat.parse(startDateText)
                val endDate = displayDateFormat.parse(endDateText)
                
                if (startDate != null && endDate != null) {
                    // Convert to API format for ViewModel
                    val startDateApi = apiDateFormat.format(startDate)
                    val endDateApi = apiDateFormat.format(endDate)
                    
                    // Validate dates
                    if (endDate.after(startDate) || endDate.equals(startDate)) {
                        // Store dates in ViewModel
                        viewModel.setDates(startDateApi, endDateApi)
                        
                        // Calculate and display duration
                        val diffInMillis = endDate.time - startDate.time
                        val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 1 // +1 to include both start and end days
                        
                        durationTextView?.text = "Trip duration: $days day${if (days > 1) "s" else ""}"
                        durationCard?.visibility = View.VISIBLE
                        
                        android.util.Log.d("TripMakerFragment", "Dates updated: $startDateApi to $endDateApi ($days days)")
                    } else {
                        // End date is before start date
                        durationCard?.visibility = View.GONE
                        android.util.Log.w("TripMakerFragment", "End date is before start date")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TripMakerFragment", "Error parsing dates: ${e.message}")
            }
        }
    }

    private fun setupCategoryStep() {
        // Set up category selection cards
        val cityBreakCard = binding.categoryStep.root.findViewById<MaterialCardView>(R.id.cityBreakCard)
        val beachCard = binding.categoryStep.root.findViewById<MaterialCardView>(R.id.beachCard)
        val mountainCard = binding.categoryStep.root.findViewById<MaterialCardView>(R.id.mountainCard)
        val roadTripCard = binding.categoryStep.root.findViewById<MaterialCardView>(R.id.roadTripCard)
        
        val cityBreakCheck = binding.categoryStep.root.findViewById<ImageView>(R.id.cityBreakCheck)
        val beachCheck = binding.categoryStep.root.findViewById<ImageView>(R.id.beachCheck)
        val mountainCheck = binding.categoryStep.root.findViewById<ImageView>(R.id.mountainCheck)
        val roadTripCheck = binding.categoryStep.root.findViewById<ImageView>(R.id.roadTripCheck)
        
        val nextButton = binding.categoryStep.root.findViewById<Button>(R.id.categoryNextButton)
        val backButton = binding.categoryStep.root.findViewById<Button>(R.id.categoryBackButton)
        
        // Helper function to clear all selections
        fun clearAllSelections() {
            cityBreakCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            beachCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            mountainCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            roadTripCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            
            cityBreakCheck?.visibility = View.GONE
            beachCheck?.visibility = View.GONE
            mountainCheck?.visibility = View.GONE
            roadTripCheck?.visibility = View.GONE
        }
        
        // Helper function to select a category
        fun selectCategory(selectedCard: MaterialCardView?, selectedCheck: ImageView?, category: String) {
            clearAllSelections()
            selectedCard?.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary)
            selectedCheck?.visibility = View.VISIBLE
            viewModel.setCategory(category)
            nextButton?.isEnabled = true
            android.util.Log.d("TripMakerFragment", "Selected category: $category")
        }
        
        // Set up click listeners for each category
        cityBreakCard?.setOnClickListener {
            selectCategory(cityBreakCard, cityBreakCheck, "City Break")
        }
        
        beachCard?.setOnClickListener {
            selectCategory(beachCard, beachCheck, "Beach")
        }
        
        mountainCard?.setOnClickListener {
            selectCategory(mountainCard, mountainCheck, "Mountain")
        }
        
        roadTripCard?.setOnClickListener {
            selectCategory(roadTripCard, roadTripCheck, "Road Trip")
        }
        
        // Navigation buttons
        nextButton?.setOnClickListener {
            if (viewModel.canProceedFromCategory()) {
                viewModel.nextStep()
            }
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
        
        // Initially disable next button until a category is selected
        nextButton?.isEnabled = false
    }

    private fun setupSeasonStep() {
        // Set up season selection cards
        val springCard = binding.seasonStep.root.findViewById<MaterialCardView>(R.id.springCard)
        val summerCard = binding.seasonStep.root.findViewById<MaterialCardView>(R.id.summerCard)
        val autumnCard = binding.seasonStep.root.findViewById<MaterialCardView>(R.id.autumnCard)
        val winterCard = binding.seasonStep.root.findViewById<MaterialCardView>(R.id.winterCard)
        
        val springCheck = binding.seasonStep.root.findViewById<ImageView>(R.id.springCheck)
        val summerCheck = binding.seasonStep.root.findViewById<ImageView>(R.id.summerCheck)
        val autumnCheck = binding.seasonStep.root.findViewById<ImageView>(R.id.autumnCheck)
        val winterCheck = binding.seasonStep.root.findViewById<ImageView>(R.id.winterCheck)
        
        val nextButton = binding.seasonStep.root.findViewById<Button>(R.id.seasonNextButton)
        val backButton = binding.seasonStep.root.findViewById<Button>(R.id.seasonBackButton)
        
        // Helper function to clear all selections
        fun clearAllSelections() {
            springCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            summerCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            autumnCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            winterCard?.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            
            springCheck?.visibility = View.GONE
            summerCheck?.visibility = View.GONE
            autumnCheck?.visibility = View.GONE
            winterCheck?.visibility = View.GONE
        }
        
        // Helper function to select a season
        fun selectSeason(selectedCard: MaterialCardView?, selectedCheck: ImageView?, season: String) {
            clearAllSelections()
            selectedCard?.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary)
            selectedCheck?.visibility = View.VISIBLE
            viewModel.setSeason(season)
            nextButton?.isEnabled = true
            android.util.Log.d("TripMakerFragment", "Selected season: $season")
        }
        
        // Set up click listeners for each season
        springCard?.setOnClickListener {
            selectSeason(springCard, springCheck, "Spring")
        }
        
        summerCard?.setOnClickListener {
            selectSeason(summerCard, summerCheck, "Summer")
        }
        
        autumnCard?.setOnClickListener {
            selectSeason(autumnCard, autumnCheck, "Autumn")
        }
        
        winterCard?.setOnClickListener {
            selectSeason(winterCard, winterCheck, "Winter")
        }
        
        // Navigation buttons
        nextButton?.setOnClickListener {
            if (viewModel.canProceedFromSeason()) {
                viewModel.nextStep() // Move to must-visit step
            }
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
        
        // Initially disable next button until a season is selected
        nextButton?.isEnabled = false
    }

    private fun setupMustVisitStep() {
        // Initialize adapters (if not already done)
        if (!::mustVisitPlacesAdapter.isInitialized) {
            mustVisitPlacesAdapter = MustVisitPlacesAdapter { place ->
                viewModel.togglePlaceSelection(place)
            }
        }
        
        if (!::selectedPlacesAdapter.isInitialized) {
            selectedPlacesAdapter = SelectedPlacesAdapter { place ->
                viewModel.removeSelectedPlace(place)
            }
        }
        
        // Set up RecyclerViews
        val suggestionsRecyclerView = binding.mustVisitStep.root.findViewById<RecyclerView>(R.id.mustVisitSuggestionsRecyclerView)
        val selectedPlacesRecyclerView = binding.mustVisitStep.root.findViewById<RecyclerView>(R.id.selectedPlacesRecyclerView)
        
        suggestionsRecyclerView?.apply {
            adapter = mustVisitPlacesAdapter
            layoutManager = LinearLayoutManager(context)
        }
        
        selectedPlacesRecyclerView?.apply {
            adapter = selectedPlacesAdapter
            layoutManager = LinearLayoutManager(context)
        }
        
        // Set up search functionality
        val searchEditText = binding.mustVisitStep.root.findViewById<TextInputEditText>(R.id.mustVisitSearchEditText)
        val loadingProgressBar = binding.mustVisitStep.root.findViewById<android.widget.ProgressBar>(R.id.mustVisitSearchLoadingProgressBar)
        val selectedPlacesCard = binding.mustVisitStep.root.findViewById<MaterialCardView>(R.id.selectedPlacesCard)
        
        // Set up category chips
        val chipAll = binding.mustVisitStep.root.findViewById<Chip>(R.id.chipAllCategories)
        val chipCultural = binding.mustVisitStep.root.findViewById<Chip>(R.id.chipCulturalSites)
        val chipMuseums = binding.mustVisitStep.root.findViewById<Chip>(R.id.chipMuseums)
        val chipEntertainment = binding.mustVisitStep.root.findViewById<Chip>(R.id.chipEntertainment)
        val chipNature = binding.mustVisitStep.root.findViewById<Chip>(R.id.chipNature)
        
        // Category filter listeners with simpler logic
        var isUpdatingChips = false
        
        fun updateCategoryFilter(category: String?) {
            if (isUpdatingChips) return
            isUpdatingChips = true
            
            // Clear all chips
            chipAll?.isChecked = false
            chipCultural?.isChecked = false
            chipMuseums?.isChecked = false
            chipEntertainment?.isChecked = false
            chipNature?.isChecked = false
            
            // Set the selected chip
            when (category) {
                null -> chipAll?.isChecked = true
                "Cultural Sites" -> chipCultural?.isChecked = true
                "Museums" -> chipMuseums?.isChecked = true
                "Entertainment" -> chipEntertainment?.isChecked = true
                "Nature" -> chipNature?.isChecked = true
            }
            
            isUpdatingChips = false
            viewModel.setCategoryFilter(category)
            Log.d("TripMakerFragment", "Selected category filter: $category")
        }
        
        chipAll?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) updateCategoryFilter(null)
        }
        
        chipCultural?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) updateCategoryFilter("Cultural Sites")
        }
        
        chipMuseums?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) updateCategoryFilter("Museums")
        }
        
        chipEntertainment?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) updateCategoryFilter("Entertainment")
        }
        
        chipNature?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) updateCategoryFilter("Nature")
        }
        
        // Initialize with All selected
        updateCategoryFilter(null)
        
        // Load initial popular places when step loads
        val selectedCity = viewModel.tripData.value?.selectedCity?.name
        if (selectedCity != null) {
            Log.d("TripMakerFragment", "Loading initial popular places for city: $selectedCity")
            viewModel.searchMustVisitPlaces(selectedCity, null, null, 10) // Load top 10 popular places
        } else {
            Log.e("TripMakerFragment", "No city selected, cannot load initial places")
        }
        
        // Search text watcher
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()
                val city = viewModel.tripData.value?.selectedCity?.name
                Log.d("TripMakerFragment", "Search query changed: '$query', city: '$city'")
                if (city != null) {
                    // Pass the current category filter when searching
                    val currentCategory = when {
                        chipCultural?.isChecked == true -> "Cultural Sites"
                        chipMuseums?.isChecked == true -> "Museums"
                        chipEntertainment?.isChecked == true -> "Entertainment"
                        chipNature?.isChecked == true -> "Nature"
                        else -> null
                    }
                    viewModel.searchMustVisitPlaces(city, query, currentCategory)
                } else {
                    Log.e("TripMakerFragment", "City is null, cannot search must-visit places")
                }
            }
        })
        
        // Navigation buttons
        val nextButton = binding.mustVisitStep.root.findViewById<Button>(R.id.mustVisitNextButton)
        val backButton = binding.mustVisitStep.root.findViewById<Button>(R.id.mustVisitBackButton)
        
        nextButton?.setOnClickListener {
            if (viewModel.canProceedFromMustVisit()) {
                viewModel.nextStep() // Move to title step
            }
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
        
        // Observe ViewModel data
        viewModel.mustVisitSearchResults.observe(viewLifecycleOwner) { places ->
            Log.d("TripMakerFragment", "OBSERVER: Received ${places.size} places from ViewModel")
            places.forEachIndexed { index, place ->
                Log.d("TripMakerFragment", "Observer place $index: ${place.name} (coords: ${place.coordinates != null}, address: ${place.address != null})")
            }
            
            Log.d("TripMakerFragment", "Updating adapter with ${places.size} places")
            mustVisitPlacesAdapter.updatePlaces(places)
            
            val visibility = if (places.isNotEmpty()) View.VISIBLE else View.GONE
            suggestionsRecyclerView?.visibility = visibility
            Log.d("TripMakerFragment", "RecyclerView visibility set to: ${if (places.isNotEmpty()) "VISIBLE" else "GONE"}")
            Log.d("TripMakerFragment", "RecyclerView current visibility: ${suggestionsRecyclerView?.visibility}")
            Log.d("TripMakerFragment", "RecyclerView adapter: ${suggestionsRecyclerView?.adapter}")
            Log.d("TripMakerFragment", "RecyclerView adapter itemCount: ${suggestionsRecyclerView?.adapter?.itemCount}")
        }
        
        viewModel.selectedMustVisitPlaces.observe(viewLifecycleOwner) { selectedPlaces ->
            selectedPlacesAdapter.updateSelectedPlaces(selectedPlaces)
            selectedPlacesCard?.visibility = if (selectedPlaces.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isMustVisitSearching.observe(viewLifecycleOwner) { isSearching ->
            loadingProgressBar?.visibility = if (isSearching) View.VISIBLE else View.GONE
        }
        
        // Load initial places for the selected city
        val city = viewModel.tripData.value?.selectedCity?.name
        if (city != null) {
            Log.d("TripMakerFragment", "Loading initial popular places for city: $city")
            viewModel.searchMustVisitPlaces(city, query = null, category = null, limit = 10) // Show 10 popular places initially
        } else {
            Log.e("TripMakerFragment", "No city selected, cannot show initial must-visit places")
        }
    }

    private fun setupTitleStep() {
        val tripTitleEditText = binding.titleStep.root.findViewById<TextInputEditText>(R.id.tripTitleEditText)
        val suggestion1Chip = binding.titleStep.root.findViewById<Chip>(R.id.suggestion1Chip)
        val suggestion2Chip = binding.titleStep.root.findViewById<Chip>(R.id.suggestion2Chip)
        val nextButton = binding.titleStep.root.findViewById<Button>(R.id.titleNextButton)
        val backButton = binding.titleStep.root.findViewById<Button>(R.id.titleBackButton)
        
        // Update summary fields
        updateTitleSummary()
        
        // Setup privacy controls
        setupTitlePrivacyControls()
        
        // Generate suggestions based on trip data
        val tripData = viewModel.tripData.value
        val cityName = tripData?.selectedCity?.name ?: "Destination"
        val season = tripData?.season ?: "Season"
        val category = tripData?.category ?: "Trip"
        
        suggestion1Chip?.text = "My $cityName Adventure"
        suggestion2Chip?.text = "$season in $cityName"
        
        // Set default title if none exists
        if (tripData?.title.isNullOrBlank()) {
            tripTitleEditText?.setText("Trip to $cityName")
        } else {
            tripTitleEditText?.setText(tripData?.title)
        }
        
        // Text watcher for real-time validation
        tripTitleEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val title = s?.toString()?.trim() ?: ""
                viewModel.setTripTitle(title)
                nextButton?.isEnabled = viewModel.canProceedFromTitle()
            }
        })
        
        // Suggestion chips
        suggestion1Chip?.setOnClickListener {
            val suggestedTitle = suggestion1Chip.text.toString()
            tripTitleEditText?.setText(suggestedTitle)
            viewModel.setTripTitle(suggestedTitle)
        }
        
        suggestion2Chip?.setOnClickListener {
            val suggestedTitle = suggestion2Chip.text.toString()
            tripTitleEditText?.setText(suggestedTitle)
            viewModel.setTripTitle(suggestedTitle)
        }
        
        // Navigation buttons
        nextButton?.setOnClickListener {
            if (viewModel.canProceedFromTitle()) {
                viewModel.createTrip() // Start the trip creation process
                viewModel.nextStep() // Move to loading step
            }
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
        
        // Initial validation
        nextButton?.isEnabled = viewModel.canProceedFromTitle()
    }
    
    private fun updateTitleSummary() {
        val tripData = viewModel.tripData.value ?: return
        
        val summaryDestinationText = binding.titleStep.root.findViewById<TextView>(R.id.summaryDestinationText)
        val summaryDatesText = binding.titleStep.root.findViewById<TextView>(R.id.summaryDatesText)
        val summaryCategorySeasonText = binding.titleStep.root.findViewById<TextView>(R.id.summaryCategorySeasonText)
        val summaryMustVisitText = binding.titleStep.root.findViewById<TextView>(R.id.summaryMustVisitText)
        
        summaryDestinationText?.text = tripData.selectedCity?.name ?: "Unknown"
        
        val startDate = tripData.startDate
        val endDate = tripData.endDate
        if (startDate != null && endDate != null) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val startFormatted = outputFormat.format(inputFormat.parse(startDate)!!)
                val endFormatted = outputFormat.format(inputFormat.parse(endDate)!!)
                summaryDatesText?.text = "$startFormatted - $endFormatted"
            } catch (e: Exception) {
                summaryDatesText?.text = "$startDate - $endDate"
            }
        }
        
        val categorySeasonText = "${tripData.category ?: "Unknown"} • ${tripData.season ?: "Unknown"}"
        summaryCategorySeasonText?.text = categorySeasonText
        
        val selectedPlacesCount = viewModel.selectedMustVisitPlaces.value?.size ?: 0
        summaryMustVisitText?.text = if (selectedPlacesCount > 0) {
            "$selectedPlacesCount selected"
        } else {
            "None selected"
        }
    }

    private fun setupLoadingStep() {
        // The loading step shows a progress indicator
        val headerText = binding.loadingStep.root.findViewById<TextView>(R.id.headerText)
        val loadingText = binding.loadingStep.root.findViewById<TextView>(R.id.loadingText)
        val progressBar = binding.loadingStep.root.findViewById<View>(R.id.progressBar)
        
        // Update loading messages based on current trip data
        val selectedCity = viewModel.tripData.value?.selectedCity?.name ?: "your destination"
        headerText?.text = "Creating Your Perfect Trip to $selectedCity"
        loadingText?.text = "Please wait while we create your personalized itinerary..."
        
        // Ensure progress bar is visible
        progressBar?.visibility = View.VISIBLE
        
        android.util.Log.d("TripMakerFragment", "Loading step initialized for $selectedCity")
    }

    private fun setupResultsStep() {
        // Set up action buttons
        val viewDetailsButton = binding.resultsStep.root.findViewById<Button>(R.id.resultsViewDetailsButton)
        val startOverButton = binding.resultsStep.root.findViewById<Button>(R.id.resultsStartOverButton)
        
        viewDetailsButton?.setOnClickListener {
            android.util.Log.d("TripMakerFragment", "🎯 View Details button clicked!")
            
            // Show trip details without navigating to step 12
            val routeResult = viewModel.routeCreationResult.value
            android.util.Log.d("TripMakerFragment", "Route result: $routeResult")
            
            if (routeResult is RouteCreationResult.Success) {
                android.util.Log.d("TripMakerFragment", "✅ Route ID found: ${routeResult.routeId}")
                showTripDetails(routeResult.routeId)
            } else {
                android.util.Log.e("TripMakerFragment", "❌ No route data available for detailed view")
                android.util.Log.e("TripMakerFragment", "Route result type: ${routeResult?.javaClass?.simpleName}")
                // Show error message
                android.widget.Toast.makeText(requireContext(), "No trip data available", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        startOverButton?.setOnClickListener {
            // Reset the trip maker and start over
            android.util.Log.d("TripMakerFragment", "🔄 Start Over clicked - resetting trip maker")
            resetTripMakerState()
        }
        
        // Populate results with actual trip data
        populateResultsWithTripData()
    }

    private fun handleRouteCreationResult(result: RouteCreationResult) {
        when (result) {
            is RouteCreationResult.Loading -> {
                android.util.Log.d("TripMakerFragment", "Trip creation in progress...")
                // Loading step is already visible, just update the UI if needed
            }
            is RouteCreationResult.Success -> {
                android.util.Log.d("TripMakerFragment", "Trip created successfully with ID: ${result.routeId}")
                // Automatically move to results step
                viewModel.nextStep()
            }
            is RouteCreationResult.Error -> {
                android.util.Log.e("TripMakerFragment", "Trip creation failed: ${result.message}")
                showTripCreationError(result.message)
            }
            is RouteCreationResult.Idle -> {
                // Do nothing for idle state
            }
        }
    }

    private fun showTripCreationError(errorMessage: String) {
        // Show error dialog or update loading step to show error
        val loadingText = binding.loadingStep.root.findViewById<TextView>(R.id.loadingText)
        val headerText = binding.loadingStep.root.findViewById<TextView>(R.id.headerText)
        val progressBar = binding.loadingStep.root.findViewById<View>(R.id.progressBar)
        
        // Update UI to show error state
        headerText?.text = "Oops! Something went wrong"
        loadingText?.text = "Error: $errorMessage\n\nPlease try again."
        progressBar?.visibility = View.GONE
        
        // Add a retry button or back button functionality
        // For now, just log the error
        android.util.Log.e("TripMakerFragment", "Showing error: $errorMessage")
    }

    private fun populateResultsWithTripData() {
        val tripData = viewModel.tripData.value
        
        if (tripData?.selectedCity == null) {
            android.util.Log.w("TripMakerFragment", "No trip data available to populate results")
            return
        }
        
        // Find all the TextViews in the results layout that need to be updated
        val resultsRoot = binding.resultsStep.root
        
        // Load trip image from first place in selected must-visit places
        val tripImageView = resultsRoot.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.resultsTripImageView)
        val selectedPlaces = viewModel.selectedMustVisitPlaces.value
        if (!selectedPlaces.isNullOrEmpty() && tripImageView != null) {
            val firstPlace = selectedPlaces.first()
            if (!firstPlace.image.isNullOrEmpty()) {
                Log.d("TripMakerFragment", "Loading trip image from first place: ${firstPlace.name} - ${firstPlace.image}")
                Glide.with(this)
                    .load(firstPlace.image)
                    .placeholder(R.drawable.ic_place_placeholder)
                    .error(R.drawable.ic_place_placeholder)
                    .into(tripImageView)
            } else {
                Log.d("TripMakerFragment", "First place has no image, using placeholder")
                tripImageView.setImageResource(R.drawable.ic_place_placeholder)
            }
        } else {
            Log.d("TripMakerFragment", "No selected places available, using placeholder")
            tripImageView?.setImageResource(R.drawable.ic_place_placeholder)
        }
        
        // Update destination
        val destinationText = resultsRoot.findViewById<TextView>(R.id.resultDestinationText)
        destinationText?.text = tripData.selectedCity?.displayText
        
        // Update dates  
        val datesText = resultsRoot.findViewById<TextView>(R.id.resultDatesText)
        if (tripData.startDate != null && tripData.endDate != null) {
            datesText?.text = "${tripData.startDate} - ${tripData.endDate}"
        }
        
        // Update duration
        val durationText = resultsRoot.findViewById<TextView>(R.id.resultDurationText)
        if (tripData.startDate != null && tripData.endDate != null) {
            try {
                val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = apiDateFormat.parse(tripData.startDate)
                val endDate = apiDateFormat.parse(tripData.endDate)
                if (startDate != null && endDate != null) {
                    val diffInMillis = endDate.time - startDate.time
                    val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 1
                    durationText?.text = "$days day${if (days > 1) "s" else ""}"
                }
            } catch (e: Exception) {
                android.util.Log.e("TripMakerFragment", "Error calculating duration: ${e.message}")
                durationText?.text = "Multiple days"
            }
        }
        
        // Update trip title in the trip card
        val tripTitleText = resultsRoot.findViewById<TextView>(R.id.resultTripTitleText)
        tripTitleText?.text = "Trip to ${tripData.selectedCity?.name}"
        
        // Update trip dates in the trip card
        val tripDatesText = resultsRoot.findViewById<TextView>(R.id.resultTripDatesText)
        if (tripData.startDate != null && tripData.endDate != null) {
            tripDatesText?.text = "${tripData.startDate} - ${tripData.endDate}"
        }
        
        android.util.Log.d("TripMakerFragment", "Results populated with trip data: ${tripData.selectedCity?.displayText}")
    }

    private fun setupInterestsStep() {
        val nextButton = binding.interestsStep.root.findViewById<Button>(R.id.interestsNextButton)
        val backButton = binding.interestsStep.root.findViewById<Button>(R.id.interestsBackButton)
        
        // Set up interest chip selection (using new category mapping chips)
        val chipMuseums = binding.interestsStep.root.findViewById<Chip>(R.id.chipMuseums)
        val chipMajorMuseums = binding.interestsStep.root.findViewById<Chip>(R.id.chipMajorMuseums)
        val chipThemeParks = binding.interestsStep.root.findViewById<Chip>(R.id.chipThemeParks)
        val chipParksNature = binding.interestsStep.root.findViewById<Chip>(R.id.chipParksNature)
        val chipZoosAquariums = binding.interestsStep.root.findViewById<Chip>(R.id.chipZoosAquariums)
        val chipSportsRecreation = binding.interestsStep.root.findViewById<Chip>(R.id.chipSportsRecreation)
        val chipEntertainment = binding.interestsStep.root.findViewById<Chip>(R.id.chipEntertainment)
        val chipWellnessRelaxation = binding.interestsStep.root.findViewById<Chip>(R.id.chipWellnessRelaxation)
        val chipCulturalSites = binding.interestsStep.root.findViewById<Chip>(R.id.chipCulturalSites)
        val chipReligiousSites = binding.interestsStep.root.findViewById<Chip>(R.id.chipReligiousSites)
        val chipShoppingMarkets = binding.interestsStep.root.findViewById<Chip>(R.id.chipShoppingMarkets)
        val chipToursActivities = binding.interestsStep.root.findViewById<Chip>(R.id.chipToursActivities)
        val chipLandmarksMonuments = binding.interestsStep.root.findViewById<Chip>(R.id.chipLandmarksMonuments)
        val chipTransportation = binding.interestsStep.root.findViewById<Chip>(R.id.chipTransportation)
        val chipOther = binding.interestsStep.root.findViewById<Chip>(R.id.chipOther)
        
        // Set up chip click listeners to collect selected interests
        val chips = listOf(
            chipMuseums, chipMajorMuseums, chipThemeParks, chipParksNature, chipZoosAquariums,
            chipSportsRecreation, chipEntertainment, chipWellnessRelaxation, chipCulturalSites,
            chipReligiousSites, chipShoppingMarkets, chipToursActivities, chipLandmarksMonuments,
            chipTransportation, chipOther
        )
        
        chips.filterNotNull().forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                Log.d("TripMakerFragment", "Interest chip ${chip.text} checked: $isChecked")
            }
        }
        
        nextButton?.setOnClickListener {
            // Collect selected interests
            val selectedInterests = chips.filterNotNull().filter { it.isChecked }.map { chip ->
                // Extract the actual interest text (remove emoji and clean up)
                chip.text.toString().replace(Regex("^[\\p{So}\\p{Cn}]+\\s*"), "").trim()
            }
            Log.d("TripMakerFragment", "Selected interests: $selectedInterests")
            
            // Store interests in ViewModel
            viewModel.setInterests(selectedInterests)
            viewModel.nextStep()
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
    }

    private fun setupTripDetailsStep() {
        // Display detailed trip information using the existing beautiful layout
        val routeResult = viewModel.routeCreationResult.value
        if (routeResult is RouteCreationResult.Success) {
            populateTripDetails(routeResult.routeId)
        }
        
        // Set up back button to go back to results
        val backButton = binding.tripDetailsStep.root.findViewById<ImageView>(R.id.tripDetailsBackButton)
        backButton?.setOnClickListener {
            viewModel.goToStep(11) // Go back to results step (now step 11)
        }
        
        // Note: Save button was removed as requested by user
        
        android.util.Log.d("TripMakerFragment", "Trip details step initialized")
    }
    
    private fun showTripDetails(routeId: String) {
        android.util.Log.d("TripMakerFragment", "🔍 showTripDetails called for route ID: $routeId")
        
        // Hide all other steps
        binding.apply {
            welcomeStep.root.visibility = View.GONE
            destinationStep.root.visibility = View.GONE
            dateStep.root.visibility = View.GONE
            categoryStep.root.visibility = View.GONE
            seasonStep.root.visibility = View.GONE
            mustVisitStep.root.visibility = View.GONE
            interestsStep.root.visibility = View.GONE
            budgetStep.root.visibility = View.GONE
            travelStyleStep.root.visibility = View.GONE
            titleStep.root.visibility = View.GONE
            loadingStep.root.visibility = View.GONE
            resultsStep.root.visibility = View.GONE
        }
        android.util.Log.d("TripMakerFragment", "✅ All other steps hidden")
        
        // Show trip details step
        binding.tripDetailsStep.root.visibility = View.VISIBLE
        android.util.Log.d("TripMakerFragment", "✅ Trip details step shown")
        
        // Hide the progress bar and step counter since this is not part of the flow
        binding.progressContainer.visibility = View.GONE
        android.util.Log.d("TripMakerFragment", "✅ Progress container hidden")
        
        // Set up the trip details step with the new approach
        setupTripDetailsStepForViewing()
        
        // Populate with route data
        populateTripDetails(routeId)
        android.util.Log.d("TripMakerFragment", "✅ showTripDetails completed")
    }
    
    private fun setupTripDetailsStepForViewing() {
        // Set up back button to return to results
        val backButton = binding.tripDetailsStep.root.findViewById<ImageView>(R.id.tripDetailsBackButton)
        backButton?.setOnClickListener {
            returnToResults()
        }
        
        android.util.Log.d("TripMakerFragment", "Trip details step initialized for viewing")
    }
    
    private fun returnToResults() {
        android.util.Log.d("TripMakerFragment", "Returning to results step")
        
        // Show the progress bar again
        binding.progressContainer.visibility = View.VISIBLE
        
        // Hide trip details
        binding.tripDetailsStep.root.visibility = View.GONE
        
        // Show results step again
        binding.resultsStep.root.visibility = View.VISIBLE
    }

    private fun setupBudgetStep() {
        val lowBudgetCard = binding.budgetStep.root.findViewById<MaterialCardView>(R.id.lowBudgetCard)
        val mediumBudgetCard = binding.budgetStep.root.findViewById<MaterialCardView>(R.id.mediumBudgetCard)
        val highBudgetCard = binding.budgetStep.root.findViewById<MaterialCardView>(R.id.highBudgetCard)
        val nextButton = binding.budgetStep.root.findViewById<Button>(R.id.budgetNextButton)
        val backButton = binding.budgetStep.root.findViewById<Button>(R.id.budgetBackButton)
        
        // Fetch user preferences when step loads
        viewModel.fetchCurrentUserPreferences()
        
        // Helper function to clear all selections
        fun clearAllSelections() {
            lowBudgetCard?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            mediumBudgetCard?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            highBudgetCard?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        
        // Helper function to select a budget
        fun selectBudget(selectedCard: MaterialCardView?, budget: String) {
            clearAllSelections()
            selectedCard?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue_700))
            viewModel.setBudget(budget)
            nextButton?.isEnabled = true
            Log.d("TripMakerFragment", "Selected budget: $budget")
        }
        
        // Set up click listeners for each budget option
        lowBudgetCard?.setOnClickListener {
            selectBudget(lowBudgetCard, "budget")
        }
        
        mediumBudgetCard?.setOnClickListener {
            selectBudget(mediumBudgetCard, "mid_range")
        }
        
        highBudgetCard?.setOnClickListener {
            selectBudget(highBudgetCard, "high")
        }
        
        // Navigation buttons
        nextButton?.setOnClickListener {
            viewModel.nextStep()
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
        
        // Initially disable next button until a budget is selected
        nextButton?.isEnabled = false
        
        // Observe user preferences and pre-select budget
        viewModel.userPreferences.observe(viewLifecycleOwner) { preferences ->
            if (preferences?.budget != null) {
                Log.d("TripMakerFragment", "Pre-selecting user's budget: ${preferences.budget}")
                when (preferences.budget) {
                    "budget" -> {
                        selectBudget(lowBudgetCard, "budget")
                        Log.d("TripMakerFragment", "Pre-selected: Budget (Low)")
                    }
                    "mid_range" -> {
                        selectBudget(mediumBudgetCard, "mid_range")
                        Log.d("TripMakerFragment", "Pre-selected: Mid-range")
                    }
                    "high" -> {
                        selectBudget(highBudgetCard, "high")
                        Log.d("TripMakerFragment", "Pre-selected: High")
                    }
                    else -> {
                        Log.d("TripMakerFragment", "Unknown budget preference: ${preferences.budget}")
                    }
                }
            } else {
                Log.d("TripMakerFragment", "No existing budget preference found")
            }
        }
    }

    private fun setupTravelStyleStep() {
        val relaxedCard = binding.travelStyleStep.root.findViewById<MaterialCardView>(R.id.relaxedModeCard)
        val moderateCard = binding.travelStyleStep.root.findViewById<MaterialCardView>(R.id.moderateModeCard)
        val acceleratedCard = binding.travelStyleStep.root.findViewById<MaterialCardView>(R.id.acceleratedModeCard)
        val nextButton = binding.travelStyleStep.root.findViewById<Button>(R.id.travelModeNextButton)
        val backButton = binding.travelStyleStep.root.findViewById<Button>(R.id.travelModeBackButton)
        
        // Fetch user preferences when step loads (if not already fetched)
        if (viewModel.userPreferences.value == null) {
            viewModel.fetchCurrentUserPreferences()
        }
        
        // Helper function to clear all selections
        fun clearAllSelections() {
            val unselectedColor = ContextCompat.getColor(requireContext(), R.color.travel_mode_unselected)
            relaxedCard?.setCardBackgroundColor(unselectedColor)
            moderateCard?.setCardBackgroundColor(unselectedColor)
            acceleratedCard?.setCardBackgroundColor(unselectedColor)
        }
        
        // Helper function to select a travel style
        fun selectTravelStyle(selectedCard: MaterialCardView?, travelStyle: String) {
            clearAllSelections()
            selectedCard?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue_700))
            viewModel.setTravelStyle(travelStyle)
            nextButton?.isEnabled = true
            Log.d("TripMakerFragment", "Selected travel style: $travelStyle")
        }
        
        // Set up click listeners for each travel style option
        relaxedCard?.setOnClickListener {
            selectTravelStyle(relaxedCard, "relaxed")
        }
        
        moderateCard?.setOnClickListener {
            selectTravelStyle(moderateCard, "moderate")
        }
        
        acceleratedCard?.setOnClickListener {
            selectTravelStyle(acceleratedCard, "accelerated")
        }
        
        nextButton?.setOnClickListener {
            // Submit user preferences after collecting all data
            viewModel.submitUserPreferences()
            
            viewModel.nextStep()
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
        
        // Initially disable next button until a travel style is selected
        nextButton?.isEnabled = false
        
        // Observe user preferences and pre-select travel style
        viewModel.userPreferences.observe(viewLifecycleOwner) { preferences ->
            if (preferences?.travelStyle != null) {
                Log.d("TripMakerFragment", "Pre-selecting user's travel style: ${preferences.travelStyle}")
                when (preferences.travelStyle) {
                    "relaxed" -> {
                        selectTravelStyle(relaxedCard, "relaxed")
                        Log.d("TripMakerFragment", "Pre-selected: Relaxed travel style")
                    }
                    "moderate" -> {
                        selectTravelStyle(moderateCard, "moderate")
                        Log.d("TripMakerFragment", "Pre-selected: Moderate travel style")
                    }
                    "accelerated" -> {
                        selectTravelStyle(acceleratedCard, "accelerated")
                        Log.d("TripMakerFragment", "Pre-selected: Accelerated travel style")
                    }
                    else -> {
                        Log.d("TripMakerFragment", "Unknown travel style preference: ${preferences.travelStyle}")
                    }
                }
            } else {
                Log.d("TripMakerFragment", "No existing travel style preference found")
            }
        }
        
        Log.d("TripMakerFragment", "Travel style step initialized")
    }

    private fun populateTripDetails(routeId: String) {
        Log.d("TripMakerFragment", "Populating trip details for route ID: $routeId")
        
        // Fetch the route details from the backend
        viewModel.fetchRouteDetail(routeId)
        
        // Observe route details and update UI when available
        viewModel.routeDetail.observe(viewLifecycleOwner) { routeDetail ->
            if (routeDetail != null) {
                Log.d("TripMakerFragment", "Route details received: ${routeDetail.title}")
                updateTripDetailsUI(routeDetail)
            } else {
                Log.e("TripMakerFragment", "Failed to load route details")
                // You could show an error message here
            }
        }
    }
    
    private fun updateTripDetailsUI(routeDetail: RouteDetail) {
        val tripDetailsRoot = binding.tripDetailsStep.root
        
        Log.d("TripMakerFragment", "🎨 updateTripDetailsUI called with route:")
        Log.d("TripMakerFragment", "  - Title: ${routeDetail.title}")
        Log.d("TripMakerFragment", "  - City: ${routeDetail.city}")
        Log.d("TripMakerFragment", "  - Start Date: ${routeDetail.startDate}")
        Log.d("TripMakerFragment", "  - End Date: ${routeDetail.endDate}")
        Log.d("TripMakerFragment", "  - Budget: ${routeDetail.budget}")
        Log.d("TripMakerFragment", "  - Days Count: ${routeDetail.days.size}")
        
        // Update basic trip information
        val tripTitleText = tripDetailsRoot.findViewById<TextView>(R.id.tripDetailsTitleText)
        val tripDatesText = tripDetailsRoot.findViewById<TextView>(R.id.tripDetailsDatesText)
        val tripDurationText = tripDetailsRoot.findViewById<TextView>(R.id.tripDetailsDurationText)
        val tripActivitiesText = tripDetailsRoot.findViewById<TextView>(R.id.tripDetailsActivitiesText)
        val tripBudgetText = tripDetailsRoot.findViewById<TextView>(R.id.tripDetailsBudgetText)
        
        Log.d("TripMakerFragment", "TextView found states:")
        Log.d("TripMakerFragment", "  - tripTitleText: ${tripTitleText != null}")
        Log.d("TripMakerFragment", "  - tripDatesText: ${tripDatesText != null}")
        Log.d("TripMakerFragment", "  - tripDurationText: ${tripDurationText != null}")
        Log.d("TripMakerFragment", "  - tripActivitiesText: ${tripActivitiesText != null}")
        Log.d("TripMakerFragment", "  - tripBudgetText: ${tripBudgetText != null}")
        
        // Update title (e.g., "Trip to Istanbul")
        val newTitle = "Trip to ${routeDetail.city}"
        tripTitleText?.text = newTitle
        Log.d("TripMakerFragment", "✅ Title updated to: $newTitle")
        
        // Update dates
        try {
            val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val startDate = apiDateFormat.parse(routeDetail.startDate)
            val endDate = apiDateFormat.parse(routeDetail.endDate)
            if (startDate != null && endDate != null) {
                tripDatesText?.text = "${displayDateFormat.format(startDate)} - ${displayDateFormat.format(endDate)}"
            } else {
                tripDatesText?.text = "${routeDetail.startDate} - ${routeDetail.endDate}"
            }
        } catch (e: Exception) {
            Log.e("TripMakerFragment", "Error formatting dates", e)
            tripDatesText?.text = "${routeDetail.startDate} - ${routeDetail.endDate}"
        }
        
        // Update duration
        try {
            val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = apiDateFormat.parse(routeDetail.startDate)
            val endDate = apiDateFormat.parse(routeDetail.endDate)
            if (startDate != null && endDate != null) {
                val diffInMillis = endDate.time - startDate.time
                val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 1
                tripDurationText?.text = "$days Day${if (days != 1L) "s" else ""}"
            }
        } catch (e: Exception) {
            Log.e("TripMakerFragment", "Error calculating duration", e)
            tripDurationText?.text = "N/A"
        }
        
        // Update activities count
        val totalActivities = routeDetail.days.sumOf { it.activities.size }
        tripActivitiesText?.text = "$totalActivities Activities"
        
        // Update budget
        val budgetDisplay = when (routeDetail.budget) {
            "budget" -> "Budget ($50-100/day)"
            "mid_range" -> "Mid-range ($100-200/day)"
            "high" -> "High ($200+/day)"
            else -> routeDetail.budget
        }
        tripBudgetText?.text = budgetDisplay
        
        // Setup privacy controls
        setupPrivacyToggle(routeDetail)
        
        // Load the daily itinerary with actual route data
        loadDailyItinerary(routeDetail)
        
        Log.d("TripMakerFragment", "✅ Trip details UI updated successfully:")
        Log.d("TripMakerFragment", "- Title: Trip to ${routeDetail.city}")
        Log.d("TripMakerFragment", "- Dates: ${tripDatesText?.text}")
        Log.d("TripMakerFragment", "- Duration: ${tripDurationText?.text}")
        Log.d("TripMakerFragment", "- Activities: $totalActivities")
        Log.d("TripMakerFragment", "- Budget: $budgetDisplay")
        Log.d("TripMakerFragment", "- Days loaded: ${routeDetail.days.size}")
    }
    
    private fun loadDailyItinerary(routeDetail: RouteDetail) {
        Log.d("TripMakerFragment", "🗓️ Loading daily itinerary with ${routeDetail.days.size} days")
        
        // Reset expanded state when loading new trip details
        expandedDays.clear()
        
        val tripDetailsRoot = binding.tripDetailsStep.root
        
        // Find the main container directly
        val mainContainer = tripDetailsRoot.findViewById<LinearLayout>(R.id.tripDetailsMainContainer)
        if (mainContainer == null) {
            Log.e("TripMakerFragment", "❌ Main container not found in trip details")
            return
        }
        
        Log.d("TripMakerFragment", "✅ Main container found with ${mainContainer.childCount} children")
        
        // Remove existing daily itinerary cards (keep only header and overview)
        // We'll remove everything after the overview card and rebuild it
        val childCount = mainContainer.childCount
        Log.d("TripMakerFragment", "Container has $childCount children")
        
        // Find where the daily itinerary starts (after the overview card)
        var dailyItineraryStartIndex = -1
        for (i in 0 until childCount) {
            val child = mainContainer.getChildAt(i)
            if (child is TextView) {
                val text = (child as? TextView)?.text?.toString()
                if (text?.contains("Daily Itinerary") == true) {
                    dailyItineraryStartIndex = i
                    break
                }
            }
        }
        
        if (dailyItineraryStartIndex == -1) {
            Log.e("TripMakerFragment", "❌ Daily Itinerary section not found")
            return
        }
        
        // Remove all views from Daily Itinerary onwards
        for (i in childCount - 1 downTo dailyItineraryStartIndex + 1) {
            mainContainer.removeViewAt(i)
        }
        
        Log.d("TripMakerFragment", "✅ Cleared existing daily itinerary content")
        
        // Add dynamic daily itinerary content (compact format)
        if (routeDetail.days.isNotEmpty()) {
            // Show first day with detailed timeline (like the image)
            val firstDay = routeDetail.days[0]
            Log.d("TripMakerFragment", "Creating detailed day 1: ${firstDay.date} with ${firstDay.activities.size} activities")
            val firstDayCard = createDetailedDayCard(firstDay, 1)
            mainContainer.addView(firstDayCard)
            
            // Show remaining days as compact summary cards
            if (routeDetail.days.size > 1) {
                routeDetail.days.drop(1).forEachIndexed { index, routeDay ->
                    val dayNumber = index + 2
                    Log.d("TripMakerFragment", "Creating summary day $dayNumber: ${routeDay.date} with ${routeDay.activities.size} activities")
                    val summaryCard = createSummaryDayCard(routeDay, dayNumber)
                    mainContainer.addView(summaryCard)
                }
            }
            
            // Only add "more days" indicator if there are actually hidden days
            // Since we show Day 1 detailed + all other days as summary cards, 
            // we don't need this indicator anymore as all days are accessible
            // Remove this section completely
        }
        
        Log.d("TripMakerFragment", "✅ Daily itinerary loaded successfully")
    }
    
    // Track expanded state for each day card
    private val expandedDays = mutableSetOf<Int>()
    
    private fun createDetailedDayCard(routeDay: RouteDay, dayNumber: Int): View {
        val context = requireContext()
        
        // Create the day card (like the first day in your image)
        val dayCard = com.google.android.material.card.MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24.dpToPx(context))
            }
            radius = 16.dpToPx(context).toFloat()
            cardElevation = 4.dpToPx(context).toFloat()
        }
        
        // Create the main container
        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dpToPx(context), 20.dpToPx(context), 20.dpToPx(context), 20.dpToPx(context))
        }
        
        // Add day header
        val dayTitle = TextView(context).apply {
            text = "${formatDate(routeDay.date)} - Day $dayNumber"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16.dpToPx(context))
            }
        }
        mainContainer.addView(dayTitle)
        
        // Create activities container (will hold both shown and hidden activities)
        val activitiesContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            tag = "activities_container_$dayNumber" // For finding later
        }
        
        // Initially show max 2 activities
        val isExpanded = expandedDays.contains(dayNumber)
        val activitiesToShow = if (isExpanded) routeDay.activities.size else minOf(2, routeDay.activities.size)
        
        routeDay.activities.take(activitiesToShow).forEach { activity ->
            val activityView = createActivityView(activity, context)
            activitiesContainer.addView(activityView)
        }
        
        mainContainer.addView(activitiesContainer)
        
        // Add "Show More/Less" button if there are more than 2 activities
        if (routeDay.activities.size > 2) {
            val toggleButton = TextView(context).apply {
                text = if (isExpanded) "Show Less" else "Show ${routeDay.activities.size - 2} More Activities"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.primary))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, 8.dpToPx(context), 0, 0)
                setOnClickListener {
                    toggleDayExpansion(dayNumber, routeDay, activitiesContainer, this, context)
                }
            }
            mainContainer.addView(toggleButton)
        }
        
        dayCard.addView(mainContainer)
        return dayCard
    }
    
    private fun createSummaryDayCard(routeDay: RouteDay, dayNumber: Int): View {
        val context = requireContext()
        
        // Create compact summary card (like Day 2 in your image)
        val dayCard = com.google.android.material.card.MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24.dpToPx(context))
            }
            radius = 16.dpToPx(context).toFloat()
            cardElevation = 4.dpToPx(context).toFloat()
            tag = "summary_day_$dayNumber" // For finding and replacing
        }
        
        // Create horizontal layout
        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dpToPx(context), 20.dpToPx(context), 20.dpToPx(context), 20.dpToPx(context))
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        
        // Left side - day info
        val leftContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        // Day title
        val dayTitle = TextView(context).apply {
            text = "${formatDate(routeDay.date)} - Day $dayNumber"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 4.dpToPx(context))
            }
        }
        
        // Activity summary (like "Vatican Museums • St. Peter's Basilica • Sistine Chapel")
        val activitySummary = routeDay.activities.take(3).joinToString(" • ") { it.placeName }
        val summaryText = TextView(context).apply {
            text = activitySummary
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }
        
        leftContainer.addView(dayTitle)
        leftContainer.addView(summaryText)
        mainContainer.addView(leftContainer)
        
        // Right side - arrow icon
        val arrowIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                24.dpToPx(context),
                24.dpToPx(context)
            )
            setImageResource(R.drawable.ic_arrow_back)
            rotation = 180f // Make it point right
            setColorFilter(ContextCompat.getColor(context, R.color.text_hint))
        }
        mainContainer.addView(arrowIcon)
        
        // Make the entire card clickable to expand
        dayCard.setOnClickListener {
            expandSummaryDay(routeDay, dayNumber)
        }
        
        dayCard.addView(mainContainer)
        return dayCard
    }
    
    private fun createMoreDaysIndicator(remainingDays: Int): View {
        val context = requireContext()
        
        // Create "+ X more days" indicator (like in your image)
        val indicator = TextView(context).apply {
            text = "+ $remainingDays more days with amazing activities"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 24.dpToPx(context))
            }
            setPadding(8.dpToPx(context), 8.dpToPx(context), 8.dpToPx(context), 8.dpToPx(context))
            // Create a simple ripple background
            val typedValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            if (typedValue.resourceId != 0) {
                background = ContextCompat.getDrawable(context, typedValue.resourceId)
            } else {
                // Fallback if resource is not available
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        }
        
        return indicator
    }
    
    private fun toggleDayExpansion(
        dayNumber: Int, 
        routeDay: RouteDay, 
        activitiesContainer: LinearLayout, 
        toggleButton: TextView, 
        context: android.content.Context
    ) {
        val isCurrentlyExpanded = expandedDays.contains(dayNumber)
        
        if (isCurrentlyExpanded) {
            // Collapse: Remove extra activities
            expandedDays.remove(dayNumber)
            while (activitiesContainer.childCount > 2) {
                activitiesContainer.removeViewAt(activitiesContainer.childCount - 1)
            }
            toggleButton.text = "Show ${routeDay.activities.size - 2} More Activities"
            Log.d("TripMakerFragment", "Day $dayNumber collapsed - showing 2/${routeDay.activities.size} activities")
        } else {
            // Expand: Add remaining activities
            expandedDays.add(dayNumber)
            routeDay.activities.drop(2).forEach { activity ->
                val activityView = createActivityView(activity, context)
                activitiesContainer.addView(activityView)
            }
            toggleButton.text = "Show Less"
            Log.d("TripMakerFragment", "Day $dayNumber expanded - showing all ${routeDay.activities.size} activities")
        }
    }
    
    private fun expandSummaryDay(routeDay: RouteDay, dayNumber: Int) {
        Log.d("TripMakerFragment", "Summary day $dayNumber clicked - converting to detailed view")
        
        // Find the main container and replace this summary card with detailed card
        val tripDetailsRoot = binding.tripDetailsStep.root
        val mainContainer = tripDetailsRoot.findViewById<LinearLayout>(R.id.tripDetailsMainContainer)
        
        if (mainContainer != null) {
            // Find the summary card and replace it with detailed card
            for (i in 0 until mainContainer.childCount) {
                val child = mainContainer.getChildAt(i)
                if (child.tag == "summary_day_$dayNumber") {
                    // Remove the summary card
                    mainContainer.removeViewAt(i)
                    
                    // Create and add detailed card at the same position
                    expandedDays.add(dayNumber) // Mark as expanded
                    val detailedCard = createDetailedDayCard(routeDay, dayNumber)
                    detailedCard.tag = "detailed_day_$dayNumber"
                    mainContainer.addView(detailedCard, i)
                    
                    Log.d("TripMakerFragment", "Replaced summary day $dayNumber with detailed view")
                    break
                }
            }
        }
    }
    
    private fun createDayCard(routeDay: RouteDay, dayNumber: Int): View {
        val context = requireContext()
        
        // Create the day card
        val dayCard = com.google.android.material.card.MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24.dpToPx(context))
            }
            radius = 16.dpToPx(context).toFloat()
            cardElevation = 4.dpToPx(context).toFloat()
        }
        
        // Create the main container
        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dpToPx(context), 20.dpToPx(context), 20.dpToPx(context), 20.dpToPx(context))
        }
        
        // Add day header
        val dayHeader = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        
        val dayTitle = TextView(context).apply {
            text = "${formatDate(routeDay.date)} - Day $dayNumber"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        dayHeader.addView(dayTitle)
        mainContainer.addView(dayHeader)
        
        // Add activities
        routeDay.activities.forEach { activity ->
            val activityView = createActivityView(activity, context)
            mainContainer.addView(activityView)
        }
        
        dayCard.addView(mainContainer)
        return dayCard
    }
    
    private fun createActivityView(activity: Activity, context: android.content.Context): View {
        val activityContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16.dpToPx(context), 0, 0)
            }
        }
        
        // Timeline dot
        val timelineDot = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16.dpToPx(context), 0)
            }
        }
        
        val dot = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(12.dpToPx(context), 12.dpToPx(context))
            background = ContextCompat.getDrawable(context, R.drawable.timeline_dot)
        }
        
        timelineDot.addView(dot)
        activityContainer.addView(timelineDot)
        
        // Activity content
        val activityContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        // Time
        val timeText = TextView(context).apply {
            text = activity.time
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        // Place name
        val placeText = TextView(context).apply {
            text = activity.placeName
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        // Notes/description
        val notesText = TextView(context).apply {
            text = activity.notes ?: "Explore this amazing location"
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            setLineSpacing(2.dpToPx(context).toFloat(), 1f)
        }
        
        activityContent.addView(timeText)
        activityContent.addView(placeText)
        activityContent.addView(notesText)
        
        activityContainer.addView(activityContent)
        
        return activityContainer
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            dateString
        }
    }
    
    // Extension function to convert dp to px
    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
    
    // ⭐ NEW: Setup privacy toggle controls
    private fun setupPrivacyToggle(routeDetail: RouteDetail) {
        val tripDetailsRoot = binding.tripDetailsStep.root
        val privacyCard = tripDetailsRoot.findViewById<com.google.android.material.card.MaterialCardView>(R.id.privacyCard)
        val privacyIcon = tripDetailsRoot.findViewById<ImageView>(R.id.privacyIcon)
        val privacyDescriptionText = tripDetailsRoot.findViewById<TextView>(R.id.privacyDescriptionText)
        val privacyToggleSwitch = tripDetailsRoot.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.privacyToggleSwitch)
        
        if (privacyIcon == null || privacyDescriptionText == null || privacyToggleSwitch == null) {
            Log.e("TripMakerFragment", "❌ Privacy controls not found in layout")
            return
        }
        
        // Store references for state management
        currentPrivacySwitch = privacyToggleSwitch
        currentPrivacyIcon = privacyIcon
        currentPrivacyDescription = privacyDescriptionText
        currentRouteId = routeDetail.routeId
        
        // Initialize UI based on current privacy state
        updatePrivacyUI(routeDetail.isPublic, privacyIcon, privacyDescriptionText, privacyToggleSwitch)
        
        // Set up switch listener using the helper function
        setupPrivacySwitchListener()
        
        // ⭐ NEW: Make entire card clickable
        privacyCard?.setOnClickListener {
            Log.d("TripMakerFragment", "🔒 Privacy card clicked - toggling switch")
            privacyToggleSwitch.isChecked = !privacyToggleSwitch.isChecked
        }
        
        Log.d("TripMakerFragment", "✅ Privacy toggle setup complete for route: ${routeDetail.routeId}")
    }
    
    private fun updatePrivacyUI(
        isPublic: Boolean, 
        privacyIcon: ImageView, 
        privacyDescriptionText: TextView, 
        privacyToggleSwitch: com.google.android.material.materialswitch.MaterialSwitch
    ) {
        if (isPublic) {
            privacyIcon.setImageResource(R.drawable.ic_public)
            privacyIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.success_green))
            privacyDescriptionText.text = "This trip is public"
            privacyToggleSwitch.isChecked = true
        } else {
            privacyIcon.setImageResource(R.drawable.ic_lock_private)
            privacyIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            privacyDescriptionText.text = "This trip is private"
            privacyToggleSwitch.isChecked = false
        }
    }
    
    // ⭐ NEW: Setup privacy controls in title step
    private fun setupTitlePrivacyControls() {
        val titleRoot = binding.titleStep.root
        val privacyCard = titleRoot.findViewById<com.google.android.material.card.MaterialCardView>(R.id.titlePrivacyCard)
        val privacyIcon = titleRoot.findViewById<ImageView>(R.id.titlePrivacyIcon)
        val privacyDescription = titleRoot.findViewById<TextView>(R.id.titlePrivacyDescription)
        val privacySwitch = titleRoot.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.titlePrivacySwitch)
        
        if (privacyIcon == null || privacyDescription == null || privacySwitch == null) {
            Log.e("TripMakerFragment", "❌ Title privacy controls not found in layout")
            return
        }
        
        // Get current privacy setting from ViewModel (default to private)
        val currentTripData = viewModel.tripData.value
        val isPublic = currentTripData?.isPublic ?: false
        
        // Initialize UI
        updateTitlePrivacyUI(isPublic, privacyIcon, privacyDescription, privacySwitch)
        
        // Set up switch listener
        privacySwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("TripMakerFragment", "🔒 Title privacy changed to: $isChecked")
            viewModel.setTripPrivacy(isChecked)
            updateTitlePrivacyUI(isChecked, privacyIcon, privacyDescription, privacySwitch)
        }
        
       
        privacyCard?.setOnClickListener {
            Log.d("TripMakerFragment", "🔒 Privacy card clicked - toggling switch")
            privacySwitch.isChecked = !privacySwitch.isChecked
        }
    }
    
    private fun updateTitlePrivacyUI(
        isPublic: Boolean,
        privacyIcon: ImageView,
        privacyDescription: TextView,
        privacySwitch: com.google.android.material.materialswitch.MaterialSwitch
    ) {
        if (isPublic) {
            privacyIcon.setImageResource(R.drawable.ic_public)
            privacyIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.success_green))
            privacyDescription.text = "Public - Others can discover this trip"
            privacySwitch.isChecked = true
        } else {
            privacyIcon.setImageResource(R.drawable.ic_lock_private)
            privacyIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            privacyDescription.text = "Private - Only you can see this trip"
            privacySwitch.isChecked = false
        }
    }

    /**
     * Public method for navigation handler to check unsaved changes
     */
    fun hasUnsavedChanges(): Boolean {
        return viewModel.hasUnsavedChanges()
    }
    
    /**
     * Public method for navigation handler to reset Trip Maker when user confirms leaving
     */
    fun resetTripMakerFromNavigation() {
        android.util.Log.d("TripMakerFragment", "🔄 Resetting Trip Maker from navigation")
        viewModel.resetFlow()
        clearUIState()
        updateStepDisplay(0) // Reset to welcome screen
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}