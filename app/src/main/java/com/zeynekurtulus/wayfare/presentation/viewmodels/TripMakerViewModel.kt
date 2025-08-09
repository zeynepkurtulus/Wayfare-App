package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.City
import com.zeynekurtulus.wayfare.domain.model.TripCreationData
import com.zeynekurtulus.wayfare.domain.model.CreateRoute
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlaceSearch
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlace
import com.zeynekurtulus.wayfare.domain.model.UserPreferences
import com.zeynekurtulus.wayfare.domain.model.RouteDetail
import com.zeynekurtulus.wayfare.domain.model.User
import com.zeynekurtulus.wayfare.data.mappers.MustVisitPlaceMapper
import com.zeynekurtulus.wayfare.domain.repository.CityRepository
import com.zeynekurtulus.wayfare.domain.repository.RouteRepository
import com.zeynekurtulus.wayfare.domain.repository.MustVisitRepository
import com.zeynekurtulus.wayfare.domain.repository.UserRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import android.util.Log

/**
 * TripMakerViewModel - Manages the step-by-step trip creation flow
 * 
 * Handles navigation between steps and stores user selections for trip creation.
 */
class TripMakerViewModel(
    private val cityRepository: CityRepository,
    private val routeRepository: RouteRepository,
    private val mustVisitRepository: MustVisitRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Current step in the flow (0-based)
    private val _currentStep = MutableLiveData(0)
    val currentStep: LiveData<Int> = _currentStep
    
    // Total number of steps in the route creation flow (ends at Results step)
    val totalSteps = 12  // 0: Welcome, 1: Destination, 2: Dates, 3: Category, 4: Season, 5: Interests, 6: Budget, 7: Travel Style, 8: Must-Visit, 9: Title, 10: Loading, 11: Results
    // Note: Trip Details (step 12) is not part of the creation flow - only accessible via "View Detailed Itinerary" button
    
    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Trip creation data
    private val _tripData = MutableLiveData<TripCreationData>()
    val tripData: LiveData<TripCreationData> = _tripData
    
    // City search functionality
    private val _citySearchResults = MutableLiveData<List<City>>()
    val citySearchResults: LiveData<List<City>> = _citySearchResults
    
    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> = _isSearching
    
    // Route creation result
    private val _routeCreationResult = MutableLiveData<RouteCreationResult>()
    val routeCreationResult: LiveData<RouteCreationResult> = _routeCreationResult
    
    // Must-Visit Places functionality
    private val _mustVisitSearchResults = MutableLiveData<List<MustVisitPlaceSearch>>()
    val mustVisitSearchResults: LiveData<List<MustVisitPlaceSearch>> = _mustVisitSearchResults
    
    private val _selectedMustVisitPlaces = MutableLiveData<List<MustVisitPlaceSearch>>()
    val selectedMustVisitPlaces: LiveData<List<MustVisitPlaceSearch>> = _selectedMustVisitPlaces
    
    private val _isMustVisitSearching = MutableLiveData(false)
    val isMustVisitSearching: LiveData<Boolean> = _isMustVisitSearching
    
    private var searchJob: Job? = null
    private var mustVisitSearchJob: Job? = null
    private var currentSelectedCategory: String? = null
    
    init {
        _tripData.value = TripCreationData()
    }
    
    /**
     * Navigate to the next step
     */
    fun nextStep() {
        val current = _currentStep.value ?: 0
        if (current < totalSteps - 1) {
            _currentStep.value = current + 1
        }
    }
    
    /**
     * Navigate to the previous step
     */
    fun previousStep() {
        val current = _currentStep.value ?: 0
        if (current > 0) {
            _currentStep.value = current - 1
        }
    }
    
    /**
     * Jump to a specific step
     */
    fun goToStep(step: Int) {
        if (step in 0 until totalSteps) {
            _currentStep.value = step
        }
    }
    
    /**
     * Reset the flow to the beginning
     */
    fun resetFlow() {
        _currentStep.value = 0
        _tripData.value = TripCreationData()
        _citySearchResults.value = emptyList()
        _routeCreationResult.value = RouteCreationResult.Idle
    }
    
    fun resetTripMaker() {
        // Reset all trip data
        _currentStep.value = 0
        _isLoading.value = false
        _routeCreationResult.value = RouteCreationResult.Idle
        
        // Clear city search
        clearCitySearch()
        
        // Reset trip data
        _tripData.value = TripCreationData()
        
        Log.d("TripMakerViewModel", "Trip maker reset to initial state")
    }
    
    // Route Detail Functions
    private val _routeDetail = MutableLiveData<RouteDetail?>()
    val routeDetail: LiveData<RouteDetail?> = _routeDetail
    
    // User Preferences Functions
    private val _userPreferences = MutableLiveData<UserPreferences?>()
    val userPreferences: LiveData<UserPreferences?> = _userPreferences
    
    fun fetchCurrentUserPreferences() {
        viewModelScope.launch {
            try {
                Log.d("TripMakerViewModel", "Fetching current user preferences")
                when (val result = userRepository.getCurrentUser()) {
                    is ApiResult.Success -> {
                        val userPreferences = result.data.preferences
                        _userPreferences.value = userPreferences
                        if (userPreferences != null) {
                            Log.d("TripMakerViewModel", "User preferences loaded - Budget: ${userPreferences.budget}, Travel Style: ${userPreferences.travelStyle}")
                        } else {
                            Log.d("TripMakerViewModel", "No existing user preferences found")
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e("TripMakerViewModel", "Failed to fetch user preferences: ${result.message}")
                        _userPreferences.value = null
                    }
                    is ApiResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                Log.e("TripMakerViewModel", "Error fetching user preferences", e)
                _userPreferences.value = null
            }
        }
    }
    
    fun fetchRouteDetail(routeId: String) {
        viewModelScope.launch {
            try {
                Log.d("TripMakerViewModel", "🔍 fetchRouteDetail called for route ID: $routeId")
                Log.d("TripMakerViewModel", "📡 Making API call to: /routes/$routeId")
                
                when (val result = routeRepository.getRoute(routeId)) {
                    is ApiResult.Success -> {
                        val route = result.data
                        _routeDetail.value = route
                        Log.d("TripMakerViewModel", "✅ Route details fetched successfully!")
                        Log.d("TripMakerViewModel", "  - Route ID: ${route.routeId}")
                        Log.d("TripMakerViewModel", "  - Title: ${route.title}")
                        Log.d("TripMakerViewModel", "  - City: ${route.city}")
                        Log.d("TripMakerViewModel", "  - Start Date: ${route.startDate}")
                        Log.d("TripMakerViewModel", "  - End Date: ${route.endDate}")
                        Log.d("TripMakerViewModel", "  - Budget: ${route.budget}")
                        Log.d("TripMakerViewModel", "  - Days Count: ${route.days.size}")
                        Log.d("TripMakerViewModel", "  - Activities Total: ${route.days.sumOf { it.activities.size }}")
                    }
                    is ApiResult.Error -> {
                        Log.e("TripMakerViewModel", "❌ Failed to fetch route details!")
                        Log.e("TripMakerViewModel", "  - Error message: ${result.message}")
                        Log.e("TripMakerViewModel", "  - Error code: ${result.code}")
                        _routeDetail.value = null
                    }
                    is ApiResult.Loading -> {
                        Log.d("TripMakerViewModel", "⏳ Route loading state")
                    }
                }
            } catch (e: Exception) {
                Log.e("TripMakerViewModel", "💥 Exception in fetchRouteDetail!", e)
                Log.e("TripMakerViewModel", "Exception details: ${e.message}")
                _routeDetail.value = null
            }
        }
    }
    
    // City Search Functions
    fun searchCities(query: String) {
        if (query.length < 2) {
            _citySearchResults.value = emptyList()
            return
        }
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(300) // Debounce
            
            try {
                when (val result = cityRepository.searchCities(query)) {
                    is ApiResult.Success -> {
                        _citySearchResults.value = result.data
                        Log.d("TripMakerViewModel", "Found ${result.data.size} cities for query: $query")
                    }
                    is ApiResult.Error -> {
                        _citySearchResults.value = emptyList()
                        Log.e("TripMakerViewModel", "City search error: ${result.message}")
                    }
                    is ApiResult.Loading -> {
                        // Loading state already handled
                    }
                }
            } catch (e: Exception) {
                _citySearchResults.value = emptyList()
                Log.e("TripMakerViewModel", "City search exception: ${e.message}")
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun clearCitySearch() {
        _citySearchResults.value = emptyList()
        searchJob?.cancel()
    }
    
    // Must-Visit Places Search Functions
    fun searchMustVisitPlaces(city: String, query: String? = null, category: String? = null, limit: Int = 5) {
        // Allow search with 1+ characters, but show all places if query is empty or has enough characters
        if (query != null && query.isNotEmpty() && query.length < 1) {
            _mustVisitSearchResults.value = emptyList()
            return
        }
        
        mustVisitSearchJob?.cancel()
        mustVisitSearchJob = viewModelScope.launch {
            _isMustVisitSearching.value = true
            delay(300) // Debounce
            
            try {
                Log.d("TripMakerViewModel", "Searching must-visit places - City: $city, Query: $query, Category: $category")
                
                when (val result = mustVisitRepository.searchMustVisitPlaces(city, query, category, limit)) {
                    is ApiResult.Success -> {
                        Log.d("TripMakerViewModel", "API SUCCESS: Received ${result.data.size} places for city: $city, query: '$query', category: $category")
                        result.data.forEachIndexed { index, place ->
                            Log.d("TripMakerViewModel", "Raw place $index: ${place.name} (ID: ${place.placeId}, coords: ${place.coordinates}, address: ${place.address})")
                        }
                        
                        val currentSelected = _selectedMustVisitPlaces.value ?: emptyList()
                        val updatedResults = result.data.map { place ->
                            place.copy(isSelected = currentSelected.any { it.placeId == place.placeId })
                        }
                        _mustVisitSearchResults.value = updatedResults
                        Log.d("TripMakerViewModel", "Setting ${updatedResults.size} places to LiveData")
                        
                        // Also log the final results
                        updatedResults.forEachIndexed { index, place ->
                            Log.d("TripMakerViewModel", "Final place $index: ${place.name} (selected: ${place.isSelected})")
                        }
                    }
                    is ApiResult.Error -> {
                        _mustVisitSearchResults.value = emptyList()
                        Log.e("TripMakerViewModel", "Must-visit search error: ${result.message}")
                    }
                    is ApiResult.Loading -> {
                        // Keep loading state active
                    }
                }
            } catch (e: Exception) {
                _mustVisitSearchResults.value = emptyList()
                Log.e("TripMakerViewModel", "Must-visit search exception: ${e.message}")
            } finally {
                _isMustVisitSearching.value = false
            }
        }
    }
    
    fun clearMustVisitSearch() {
        mustVisitSearchJob?.cancel()
        _mustVisitSearchResults.value = emptyList()
        _isMustVisitSearching.value = false
    }
    
    fun togglePlaceSelection(place: MustVisitPlaceSearch) {
        val currentSelected = _selectedMustVisitPlaces.value?.toMutableList() ?: mutableListOf()
        val currentSearchResults = _mustVisitSearchResults.value?.toMutableList() ?: mutableListOf()
        
        if (place.isSelected) {
            // Remove from selected
            currentSelected.removeAll { it.placeId == place.placeId }
            // Update search results
            currentSearchResults.forEach { 
                if (it.placeId == place.placeId) it.isSelected = false 
            }
            Log.d("TripMakerViewModel", "Removed place: ${place.name}")
        } else {
            // Add to selected
            val updatedPlace = place.copy(isSelected = true)
            currentSelected.add(updatedPlace)
            // Update search results
            currentSearchResults.forEach { 
                if (it.placeId == place.placeId) it.isSelected = true 
            }
            Log.d("TripMakerViewModel", "Added place: ${place.name}")
            Log.d("TripMakerViewModel", "Place image URL when selected: '${updatedPlace.image}'")
            Log.d("TripMakerViewModel", "Original place image URL: '${place.image}'")
        }
        
        _selectedMustVisitPlaces.value = currentSelected
        _mustVisitSearchResults.value = currentSearchResults
        Log.d("TripMakerViewModel", "Total selected places: ${currentSelected.size}")
    }
    
    fun removeSelectedPlace(place: MustVisitPlaceSearch) {
        val currentSelected = _selectedMustVisitPlaces.value?.toMutableList() ?: mutableListOf()
        val currentSearchResults = _mustVisitSearchResults.value?.toMutableList() ?: mutableListOf()
        
        currentSelected.removeAll { it.placeId == place.placeId }
        currentSearchResults.forEach { 
            if (it.placeId == place.placeId) it.isSelected = false 
        }
        
        _selectedMustVisitPlaces.value = currentSelected
        _mustVisitSearchResults.value = currentSearchResults
        Log.d("TripMakerViewModel", "Removed place: ${place.name}, Remaining: ${currentSelected.size}")
    }
    
    fun setCategoryFilter(category: String?) {
        currentSelectedCategory = category
        val city = _tripData.value?.selectedCity?.name
        if (city != null) {
            searchMustVisitPlaces(city, null, category)
        }
    }
    
    // Trip Data Management
    fun setSelectedCity(city: City) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(selectedCity = city)
        Log.d("TripMakerViewModel", "Selected city: ${city.displayText}")
    }
    
    fun setDates(startDate: String, endDate: String) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(startDate = startDate, endDate = endDate)
        Log.d("TripMakerViewModel", "Set dates: $startDate to $endDate")
    }
    
    fun setCategory(category: String) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(category = category)
        Log.d("TripMakerViewModel", "Selected category: $category")
    }
    
    fun setSeason(season: String) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(season = season)
        Log.d("TripMakerViewModel", "Selected season: $season")
    }
    
    fun setInterests(interests: List<String>) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(interests = interests)
        Log.d("TripMakerViewModel", "Selected interests: $interests")
    }
    
    fun setBudget(budget: String) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(budget = budget)
        Log.d("TripMakerViewModel", "Selected budget: $budget")
    }
    
    fun setTravelStyle(travelStyle: String) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(travelStyle = travelStyle)
        Log.d("TripMakerViewModel", "Selected travel style: $travelStyle")
    }
    
    // Submit user preferences to backend
    fun submitUserPreferences() {
        val tripData = _tripData.value ?: return
        
        // Check if we have all required preference data
        if (tripData.interests.isEmpty() || tripData.budget.isNullOrEmpty() || 
            tripData.travelStyle.isNullOrEmpty() || tripData.selectedCity == null) {
            Log.d("TripMakerViewModel", "Missing preference data, skipping submission")
            return
        }
        
        viewModelScope.launch {
            try {
                val preferences = UserPreferences(
                    interests = tripData.interests,
                    budget = tripData.budget!!,
                    travelStyle = tripData.travelStyle!!
                )
                
                val homeCity = tripData.selectedCity!!.name
                
                when (val result = userRepository.addUserInfo(preferences, homeCity)) {
                    is ApiResult.Success<*> -> {
                        Log.d("TripMakerViewModel", "User preferences submitted successfully")
                    }
                    is ApiResult.Error -> {
                        Log.e("TripMakerViewModel", "Failed to submit user preferences: ${result.message}")
                    }
                    is ApiResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                Log.e("TripMakerViewModel", "Error submitting user preferences", e)
            }
        }
    }
    
    // Trip Creation
    fun createTrip() {
        val tripData = _tripData.value
        
        val selectedCity = tripData?.selectedCity
        val startDate = tripData?.startDate
        val endDate = tripData?.endDate
        val category = tripData?.category
        val season = tripData?.season
        
        if (selectedCity == null || startDate == null || 
            endDate == null || category == null || season == null) {
            _routeCreationResult.value = RouteCreationResult.Error("Missing required trip information")
            return
        }
        
        _isLoading.value = true
        _routeCreationResult.value = RouteCreationResult.Loading
        
        viewModelScope.launch {
            try {
                val title = _tripData.value?.title ?: "Trip to ${selectedCity.name}"
                
                // Convert selected places to MustVisitPlace objects
                val selectedPlaces = _selectedMustVisitPlaces.value ?: emptyList()
                val mustVisitPlaces = selectedPlaces.map { place ->
                    MustVisitPlaceMapper.mapToMustVisitPlace(place)
                }
                
                val createRoute = CreateRoute(
                    title = title,
                    city = selectedCity.name,
                    startDate = startDate,
                    endDate = endDate,
                    category = category,
                    season = season,
                    mustVisit = mustVisitPlaces,
                    isPublic = tripData.isPublic  // ⭐ NEW: Include privacy setting
                )
                
                Log.d("TripMakerViewModel", "Creating trip with data: $createRoute")
                
                when (val result = routeRepository.createRoute(createRoute)) {
                    is ApiResult.Success -> {
                        _routeCreationResult.value = RouteCreationResult.Success(result.data)
                        Log.d("TripMakerViewModel", "Trip created successfully: ${result.data}")
                    }
                    is ApiResult.Error -> {
                        _routeCreationResult.value = RouteCreationResult.Error(result.message)
                        Log.e("TripMakerViewModel", "Trip creation error: ${result.message}")
                    }
                    is ApiResult.Loading -> {
                        // Already handled
                    }
                }
            } catch (e: Exception) {
                _routeCreationResult.value = RouteCreationResult.Error("Failed to create trip: ${e.message}")
                Log.e("TripMakerViewModel", "Trip creation exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Validation helpers
    fun canProceedFromDestination(): Boolean {
        return _tripData.value?.selectedCity != null
    }
    
    fun canProceedFromDates(): Boolean {
        val data = _tripData.value
        return data?.startDate != null && data.endDate != null
    }
    
    fun canProceedFromCategory(): Boolean {
        return _tripData.value?.category != null
    }
    
    fun canProceedFromSeason(): Boolean {
        return _tripData.value?.season != null
    }
    
    fun canProceedFromMustVisit(): Boolean {
        // Must-visit places are optional, user can proceed without selecting any
        return true
    }
    
    fun setTripTitle(title: String) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(title = title)
        Log.d("TripMakerViewModel", "Set trip title: $title")
    }
    
    // ⭐ NEW: Set trip privacy setting
    fun setTripPrivacy(isPublic: Boolean) {
        val currentData = _tripData.value ?: TripCreationData()
        _tripData.value = currentData.copy(isPublic = isPublic)
        Log.d("TripMakerViewModel", "Set trip privacy: isPublic = $isPublic")
    }
    
    fun canProceedFromTitle(): Boolean {
        val title = _tripData.value?.title
        return !title.isNullOrBlank() && title.trim().length >= 3
    }
}

// Result states for route creation
sealed class RouteCreationResult {
    object Idle : RouteCreationResult()
    object Loading : RouteCreationResult()
    data class Success(val routeId: String) : RouteCreationResult()
    data class Error(val message: String) : RouteCreationResult()
}