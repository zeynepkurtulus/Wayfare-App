package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.City
import com.zeynekurtulus.wayfare.domain.model.TripCreationData
import com.zeynekurtulus.wayfare.domain.model.CreateRoute
import com.zeynekurtulus.wayfare.domain.repository.CityRepository
import com.zeynekurtulus.wayfare.domain.repository.RouteRepository
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
    private val routeRepository: RouteRepository
) : ViewModel() {
    
    // Current step in the flow (0-based)
    private val _currentStep = MutableLiveData(0)
    val currentStep: LiveData<Int> = _currentStep
    
    // Total number of steps - Updated to include new steps
    val totalSteps = 7  // 0: Welcome, 1: Destination, 2: Dates, 3: Category, 4: Season, 5: Loading, 6: Results
    
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
    
    private var searchJob: Job? = null
    
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
                val title = "Trip to ${selectedCity.name}"
                val createRoute = CreateRoute(
                    title = title,
                    city = selectedCity.name,
                    startDate = startDate,
                    endDate = endDate,
                    category = category,
                    season = season
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
}

// Result states for route creation
sealed class RouteCreationResult {
    object Idle : RouteCreationResult()
    object Loading : RouteCreationResult()
    data class Success(val routeId: String) : RouteCreationResult()
    data class Error(val message: String) : RouteCreationResult()
}