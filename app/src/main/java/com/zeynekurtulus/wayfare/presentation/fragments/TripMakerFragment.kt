package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentTripMakerBinding
import com.zeynekurtulus.wayfare.domain.model.City
import com.zeynekurtulus.wayfare.presentation.adapters.CitySuggestionsAdapter
import com.zeynekurtulus.wayfare.presentation.viewmodels.TripMakerViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer

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

    private lateinit var citySuggestionsAdapter: CitySuggestionsAdapter

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
    }

    private fun setupStepFlow() {
        updateStepDisplay(0)
    }

    private fun updateStepDisplay(step: Int) {
        // Update step counter
        val stepText = binding.stepText
        stepText.text = "Step ${step + 1} of ${viewModel.totalSteps}"
        
        binding.apply {
            welcomeStep.root.visibility = View.GONE
            destinationStep.root.visibility = View.GONE
            dateStep.root.visibility = View.GONE
            categoryStep.root.visibility = View.GONE
            seasonStep.root.visibility = View.GONE
            loadingStep.root.visibility = View.GONE
            resultsStep.root.visibility = View.GONE
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
                binding.loadingStep.root.visibility = View.VISIBLE
                setupLoadingStep()
            }
            6 -> {
                binding.resultsStep.root.visibility = View.VISIBLE
                setupResultsStep()
            }
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
        
        // Date format for display
        val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Start date picker
        startDateButton?.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select start date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            
            datePicker.show(parentFragmentManager, "START_DATE_PICKER")
            
            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Date(selection)
                val displayDate = displayDateFormat.format(selectedDate)
                val apiDate = apiDateFormat.format(selectedDate)
                
                startDateTextView?.text = displayDate
                android.util.Log.d("TripMakerFragment", "Start date selected: $apiDate")
                
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
                val selectedDate = Date(selection)
                val displayDate = displayDateFormat.format(selectedDate)
                val apiDate = apiDateFormat.format(selectedDate)
                
                endDateTextView?.text = displayDate
                android.util.Log.d("TripMakerFragment", "End date selected: $apiDate")
                
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
                viewModel.createTrip() // Start the trip creation process
                viewModel.nextStep() // Move to loading step
            }
        }
        
        backButton?.setOnClickListener {
            viewModel.previousStep()
        }
        
        // Initially disable next button until a season is selected
        nextButton?.isEnabled = false
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
            // Navigate to trip details (you can implement this based on your navigation setup)
            android.util.Log.d("TripMakerFragment", "View Details clicked")
            // For now, just go back to home
            activity?.onBackPressed()
        }
        
        startOverButton?.setOnClickListener {
            // Reset the trip maker and start over
            viewModel.resetTripMaker()
            android.util.Log.d("TripMakerFragment", "Start Over clicked")
        }
        
        // Populate results with actual trip data
        populateResultsWithTripData()
    }

    private fun handleRouteCreationResult(result: TripMakerViewModel.RouteCreationResult) {
        when (result) {
            is TripMakerViewModel.RouteCreationResult.Loading -> {
                android.util.Log.d("TripMakerFragment", "Trip creation in progress...")
                // Loading step is already visible, just update the UI if needed
            }
            is TripMakerViewModel.RouteCreationResult.Success -> {
                android.util.Log.d("TripMakerFragment", "Trip created successfully with ID: ${result.routeId}")
                // Automatically move to results step
                viewModel.nextStep()
            }
            is TripMakerViewModel.RouteCreationResult.Error -> {
                android.util.Log.e("TripMakerFragment", "Trip creation failed: ${result.message}")
                showTripCreationError(result.message)
            }
            is TripMakerViewModel.RouteCreationResult.Idle -> {
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
        
        // Update destination
        val destinationText = resultsRoot.findViewById<TextView>(R.id.resultDestinationText)
        destinationText?.text = tripData.selectedCity.displayText
        
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
        val tripTitleText = resultsRoot.findViewById<TextView>(R.id.tripTitleText)
        tripTitleText?.text = "Trip to ${tripData.selectedCity.name}"
        
        // Update trip dates in the trip card
        val tripDatesText = resultsRoot.findViewById<TextView>(R.id.tripDatesText)
        if (tripData.startDate != null && tripData.endDate != null) {
            tripDatesText?.text = "${tripData.startDate} - ${tripData.endDate}"
        }
        
        android.util.Log.d("TripMakerFragment", "Results populated with trip data: ${tripData.selectedCity.displayText}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}