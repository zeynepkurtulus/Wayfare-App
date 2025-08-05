package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.PlaceRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaceViewModel(
    private val placeRepository: PlaceRepository
) : ViewModel() {
    
    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places
    
    private val _autocompleteSuggestions = MutableLiveData<List<AutocompletePlace>>()
    val autocompleteSuggestions: LiveData<List<AutocompletePlace>> = _autocompleteSuggestions
    
    private val _topRatedPlaces = MutableLiveData<List<TopRatedPlace>>()
    val topRatedPlaces: LiveData<List<TopRatedPlace>> = _topRatedPlaces
    
    private val _isLoadingTopRated = MutableLiveData<Boolean>()
    val isLoadingTopRated: LiveData<Boolean> = _isLoadingTopRated
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _isLoadingAutocomplete = MutableLiveData<Boolean>()
    val isLoadingAutocomplete: LiveData<Boolean> = _isLoadingAutocomplete
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = _selectedPlace
    
    private var searchJob: Job? = null
    private var autocompleteJob: Job? = null
    
    fun getPlacesByCity(city: String) {
        if (city.isBlank()) {
            _error.value = "Please enter a city name"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = placeRepository.getPlacesByCity(city)) {
                is ApiResult.Success -> {
                    _places.value = result.data
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun searchPlaces(
        query: String,
        city: String,
        category: String? = null,
        limit: Int = 20
    ) {
        if (query.isBlank()) {
            _places.value = emptyList()
            return
        }
        
        // Cancel previous search
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            
            _isLoading.value = true
            _error.value = null
            
            val searchPlaces = SearchPlaces(query, city, category, limit)
            when (val result = placeRepository.searchPlaces(searchPlaces)) {
                is ApiResult.Success -> {
                    _places.value = result.data
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun getAutocompleteSuggestions(
        query: String,
        city: String,
        limit: Int = 5
    ) {
        if (query.length < 2) {
            _autocompleteSuggestions.value = emptyList()
            return
        }
        
        // Cancel previous autocomplete request
        autocompleteJob?.cancel()
        
        autocompleteJob = viewModelScope.launch {
            delay(200) // Debounce autocomplete
            
            _isLoadingAutocomplete.value = true
            
            val autocompletePlaces = AutocompletePlaces(query, city, limit)
            when (val result = placeRepository.autocompletePlaces(autocompletePlaces)) {
                is ApiResult.Success -> {
                    _autocompleteSuggestions.value = result.data
                }
                is ApiResult.Error -> {
                    // Don't show error for autocomplete failures
                    _autocompleteSuggestions.value = emptyList()
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoadingAutocomplete.value = false
        }
    }
    
    fun getPlacesById(placeIds: List<String>) {
        if (placeIds.isEmpty()) return
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = placeRepository.getPlacesById(placeIds)) {
                is ApiResult.Success -> {
                    _places.value = result.data
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun selectPlace(place: Place) {
        _selectedPlace.value = place
    }
    
    fun clearSelectedPlace() {
        _selectedPlace.value = null
    }
    
    fun clearPlaces() {
        _places.value = emptyList()
    }
    
    fun clearAutocompleteSuggestions() {
        _autocompleteSuggestions.value = emptyList()
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun getPlaceById(placeId: String): Place? {
        return _places.value?.find { it.placeId == placeId }
    }
    
    fun getTopRatedPlaces() {
        _isLoadingTopRated.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = placeRepository.getTopRatedPlaces()) {
                is ApiResult.Success -> {
                    _topRatedPlaces.value = result.data
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoadingTopRated.value = false
        }
    }
    
    fun clearTopRatedPlaces() {
        _topRatedPlaces.value = emptyList()
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        autocompleteJob?.cancel()
    }
}