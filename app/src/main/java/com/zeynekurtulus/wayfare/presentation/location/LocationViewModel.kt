package com.zeynekurtulus.wayfare.presentation.location

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
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _selectedCity = MutableLiveData<LocationCity?>()
    val selectedCity: LiveData<LocationCity?> = _selectedCity
    
    private val _selectedCountry = MutableLiveData<Country?>()
    val selectedCountry: LiveData<Country?> = _selectedCountry
    
    fun getAllCities() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getAllCities()) {
                is ApiResult.Success -> {
                    _cities.value = result.data.filter { it.active }
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
    
    fun getCitiesByCountry(country: String) {
        if (country.isBlank()) {
            _error.value = "Please select a country"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getCitiesByCountry(country)) {
                is ApiResult.Success -> {
                    _cities.value = result.data.filter { it.active }
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
    
    fun getAllCountries() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getAllCountries()) {
                is ApiResult.Success -> {
                    _countries.value = result.data.filter { it.active }
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
    
    fun getCountriesByRegion(region: String) {
        if (region.isBlank()) {
            _error.value = "Please select a region"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getCountriesByRegion(region)) {
                is ApiResult.Success -> {
                    _countries.value = result.data.filter { it.active }
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
    
    fun searchCountries(query: String, limit: Int = 10) {
        if (query.length < 2) {
            _countries.value = emptyList()
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            val searchCountries = SearchCountries(query, limit)
            when (val result = locationRepository.searchCountries(searchCountries)) {
                is ApiResult.Success -> {
                    _countries.value = result.data.filter { it.active }
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
    
    fun getAllRegions() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = locationRepository.getAllRegions()) {
                is ApiResult.Success -> {
                    _regions.value = result.data
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
    
    fun selectCity(city: LocationCity) {
        _selectedCity.value = city
    }
    
    fun selectCountry(country: Country) {
        _selectedCountry.value = country
        // Auto-load cities for selected country
        getCitiesByCountry(country.name)
    }
    
    fun clearSelectedCity() {
        _selectedCity.value = null
    }
    
    fun clearSelectedCountry() {
        _selectedCountry.value = null
        _cities.value = emptyList()
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun getCityById(cityId: String): LocationCity? {
        return _cities.value?.find { it.cityId == cityId }
    }
    
    fun getCountryById(countryId: String): Country? {
        return _countries.value?.find { it.countryId == countryId }
    }
    
    fun getCitiesByCountryId(countryId: String): List<LocationCity> {
        return _cities.value?.filter { it.countryId == countryId } ?: emptyList()
    }
}