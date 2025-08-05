package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.LocationRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import kotlinx.coroutines.launch

class LocationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {
    
    private val _cities = MutableLiveData<List<LocationCity>>()
    val cities: LiveData<List<LocationCity>> = _cities
    
    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> = _countries
    
    private val _regions = MutableLiveData<List<String>>()
    val regions: LiveData<List<String>> = _regions
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _selectedCity = MutableLiveData<LocationCity?>()
    val selectedCity: LiveData<LocationCity?> = _selectedCity
    
    private val _selectedCountry = MutableLiveData<Country?>()
    val selectedCountry: LiveData<Country?> = _selectedCountry
    
    private val _selectedRegion = MutableLiveData<String?>()
    val selectedRegion: LiveData<String?> = _selectedRegion

    fun loadAllCities() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getAllCities()) {
                is ApiResult.Success -> {
                    _cities.value = result.data
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
    
    fun loadCitiesByCountry(country: String) {
        if (country.isBlank()) {
            _error.value = "Country cannot be empty"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getCitiesByCountry(country)) {
                is ApiResult.Success -> {
                    _cities.value = result.data
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

    fun loadAllCountries() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getAllCountries()) {
                is ApiResult.Success -> {
                    _countries.value = result.data
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

    fun searchCities(query: String) {
        // For now, just filter existing cities
        val currentCities = _cities.value ?: return
        val filtered = currentCities.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.country.contains(query, ignoreCase = true)
        }
        _cities.value = filtered
    }

    fun selectCity(city: LocationCity) {
        _selectedCity.value = city
    }

    fun selectCountry(country: Country) {
        _selectedCountry.value = country
    }

    fun selectRegion(region: String) {
        _selectedRegion.value = region
    }

    fun clearSelection() {
        _selectedCity.value = null
        _selectedCountry.value = null
        _selectedRegion.value = null
    }

    fun clearError() {
        _error.value = null
    }
}