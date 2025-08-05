package com.zeynekurtulus.wayfare.presentation.viewmodels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.RouteRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.ValidationUtils
import kotlinx.coroutines.launch

class RouteDetailViewModel(
    private val routeRepository: RouteRepository
) : ViewModel() {
    
    private val _routeDetail = MutableLiveData<RouteDetail?>()
    val routeDetail: LiveData<RouteDetail?> = _routeDetail
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _createRouteState = MutableLiveData<CreateRouteState>()
    val createRouteState: LiveData<CreateRouteState> = _createRouteState
    
    private val _updateRouteState = MutableLiveData<UpdateRouteState>()
    val updateRouteState: LiveData<UpdateRouteState> = _updateRouteState
    
    private val _formErrors = MutableLiveData<RouteFormErrors>()
    val formErrors: LiveData<RouteFormErrors> = _formErrors
    
    fun loadRouteDetail(routeId: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = routeRepository.getRoute(routeId)) {
                is ApiResult.Success -> {
                    _routeDetail.value = result.data
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
    
    fun createRoute(
        title: String,
        city: String,
        startDate: String,
        endDate: String,
        category: String,
        season: String,
        mustVisit: List<MustVisitPlace> = emptyList(),
        days: List<RouteDay> = emptyList()
    ) {
        // Clear previous errors
        _formErrors.value = RouteFormErrors()
        
        // Validate input
        val validation = validateRouteInput(title, city, startDate, endDate, category, season)
        if (!validation.isValid) {
            _formErrors.value = validation.errors
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
                    val createRoute = CreateRoute(
            title = title,
            city = city,
            startDate = startDate,
            endDate = endDate,
            category = category,
            season = season
        )
            
            when (val result = routeRepository.createRoute(createRoute)) {
                is ApiResult.Success -> {
                    _createRouteState.value = CreateRouteState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _createRouteState.value = CreateRouteState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun updateRoute(
        routeId: String,
        title: String? = null,
        city: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        category: String? = null,
        season: String? = null,
        mustVisit: List<MustVisitPlace>? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val updateRoute = UpdateRoute(
                title = title,
                city = city,
                startDate = startDate,
                endDate = endDate,
                category = category,
                season = season,
                mustVisit = mustVisit
            )
            
            when (val result = routeRepository.updateRoute(routeId, updateRoute)) {
                is ApiResult.Success -> {
                    _updateRouteState.value = UpdateRouteState.Success
                    // Reload route detail
                    loadRouteDetail(routeId)
                }
                is ApiResult.Error -> {
                    _updateRouteState.value = UpdateRouteState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    private fun validateRouteInput(
        title: String,
        city: String,
        startDate: String,
        endDate: String,
        category: String,
        season: String
    ): RouteValidationResult {
        var isValid = true
        var titleError: String? = null
        var cityError: String? = null
        var categoryError: String? = null
        var seasonError: String? = null
        var dateError: String? = null
        
        if (!ValidationUtils.isValidRouteTitle(title)) {
            titleError = "Title must be between 1-100 characters"
            isValid = false
        }
        
        if (!ValidationUtils.isValidCityName(city)) {
            cityError = "Please enter a valid city name"
            isValid = false
        }
        
        if (!ValidationUtils.isValidCategory(category)) {
            categoryError = "Please select a valid category"
            isValid = false
        }
        
        if (!ValidationUtils.isValidSeason(season)) {
            seasonError = "Please select a valid season"
            isValid = false
        }
        
        if (!ValidationUtils.isValidDateRange(startDate, endDate)) {
            dateError = "End date must be after start date"
            isValid = false
        }
        
        val errors = RouteFormErrors(
            titleError = titleError,
            cityError = cityError,
            categoryError = categoryError,
            seasonError = seasonError,
            dateError = dateError
        )
        
        return RouteValidationResult(isValid, errors)
    }
    
    fun clearStates() {
        _createRouteState.value = CreateRouteState.Idle
        _updateRouteState.value = UpdateRouteState.Idle
        _formErrors.value = RouteFormErrors()
        _error.value = null
    }
    
    fun getCurrentRoute(): RouteDetail? {
        return _routeDetail.value
    }
}

sealed class CreateRouteState {
    object Idle : CreateRouteState()
    data class Success(val routeId: String) : CreateRouteState()
    data class Error(val message: String) : CreateRouteState()
}

sealed class UpdateRouteState {
    object Idle : UpdateRouteState()
    object Success : UpdateRouteState()
    data class Error(val message: String) : UpdateRouteState()
}

data class RouteFormErrors(
    val titleError: String? = null,
    val cityError: String? = null,
    val categoryError: String? = null,
    val seasonError: String? = null,
    val dateError: String? = null
)

data class RouteValidationResult(
    val isValid: Boolean,
    val errors: RouteFormErrors
)