package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.domain.model.RouteSearchParams
import com.zeynekurtulus.wayfare.domain.repository.RouteRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import kotlinx.coroutines.launch

class RouteListViewModel(
    private val routeRepository: RouteRepository
) : ViewModel() {
    
    private val _userRoutes = MutableLiveData<List<Route>>()
    val userRoutes: LiveData<List<Route>> = _userRoutes
    
    private val _publicRoutes = MutableLiveData<List<Route>>()
    val publicRoutes: LiveData<List<Route>> = _publicRoutes
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _isLoadingPublic = MutableLiveData<Boolean>()
    val isLoadingPublic: LiveData<Boolean> = _isLoadingPublic
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _deleteState = MutableLiveData<DeleteRouteState>()
    val deleteState: LiveData<DeleteRouteState> = _deleteState
    
    private val _privacyToggleState = MutableLiveData<PrivacyToggleState>()
    val privacyToggleState: LiveData<PrivacyToggleState> = _privacyToggleState
    
    private val _searchResults = MutableLiveData<List<Route>>()
    val searchResults: LiveData<List<Route>> = _searchResults
    
    init {
        android.util.Log.d("RouteListViewModel", "🔧 ViewModel CREATED - Starting automatic loadUserRoutes()")
        loadUserRoutes()
    }
    
    fun loadUserRoutes() {
        android.util.Log.d("RouteListViewModel", "📞 loadUserRoutes() CALLED")
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = routeRepository.getUserRoutes()) {
                is ApiResult.Success -> {
                    _userRoutes.value = result.data
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
    
    fun loadPublicRoutes(
        category: String? = null,
        season: String? = null,
        budget: String? = null,
        limit: Int = 20
    ) {
        _isLoadingPublic.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = routeRepository.getPublicRoutes(category, season, budget, limit)) {
                is ApiResult.Success -> {
                    _publicRoutes.value = result.data
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoadingPublic.value = false
        }
    }
    
    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteRouteState.Loading
            when (val result = routeRepository.deleteRoute(routeId)) {
                is ApiResult.Success -> {
                    _deleteState.value = DeleteRouteState.Success(routeId)
                    // Remove from current list
                    _userRoutes.value = _userRoutes.value?.filter { it.routeId != routeId }
                }
                is ApiResult.Error -> {
                    _deleteState.value = DeleteRouteState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }
    
    fun refreshUserRoutes() {
        loadUserRoutes()
    }
    
    fun refreshPublicRoutes(
        category: String? = null,
        season: String? = null,
        budget: String? = null
    ) {
        loadPublicRoutes(category, season, budget)
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearDeleteState() {
        _deleteState.value = DeleteRouteState.Idle
    }
    
    fun getRouteById(routeId: String): Route? {
        return _userRoutes.value?.find { it.routeId == routeId }
            ?: _publicRoutes.value?.find { it.routeId == routeId }
    }
    
    // ⭐ NEW: Privacy toggle functionality
    fun toggleRoutePrivacy(routeId: String, isPublic: Boolean) {
        viewModelScope.launch {
            _privacyToggleState.value = PrivacyToggleState.Loading
            android.util.Log.d("RouteListViewModel", "🔒 Toggling privacy for route $routeId to isPublic: $isPublic")
            
            when (val result = routeRepository.toggleRoutePrivacy(routeId, isPublic)) {
                is ApiResult.Success -> {
                    _privacyToggleState.value = PrivacyToggleState.Success(routeId, isPublic)
                    
                    // Update the route in the current list
                    _userRoutes.value = _userRoutes.value?.map { route ->
                        if (route.routeId == routeId) {
                            route.copy(isPublic = isPublic)
                        } else {
                            route
                        }
                    }
                    
                    android.util.Log.d("RouteListViewModel", "✅ Privacy toggled successfully")
                }
                is ApiResult.Error -> {
                    _privacyToggleState.value = PrivacyToggleState.Error(result.message)
                    android.util.Log.e("RouteListViewModel", "❌ Privacy toggle failed: ${result.message}")
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }
    
    // ⭐ NEW: Advanced route search functionality
    fun searchPublicRoutes(searchParams: RouteSearchParams) {
        _isLoadingPublic.value = true
        _error.value = null
        
        viewModelScope.launch {
            android.util.Log.d("RouteListViewModel", "🔍 Searching public routes with params: $searchParams")
            
            when (val result = routeRepository.searchPublicRoutes(searchParams)) {
                is ApiResult.Success -> {
                    _searchResults.value = result.data
                    android.util.Log.d("RouteListViewModel", "✅ Found ${result.data.size} public routes")
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    android.util.Log.e("RouteListViewModel", "❌ Search failed: ${result.message}")
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoadingPublic.value = false
        }
    }
    
    fun clearPrivacyToggleState() {
        _privacyToggleState.value = PrivacyToggleState.Idle
    }
    
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
}

sealed class DeleteRouteState {
    object Idle : DeleteRouteState()
    object Loading : DeleteRouteState()
    data class Success(val routeId: String) : DeleteRouteState()
    data class Error(val message: String) : DeleteRouteState()
}

// ⭐ NEW: Privacy toggle state management
sealed class PrivacyToggleState {
    object Idle : PrivacyToggleState()
    object Loading : PrivacyToggleState()
    data class Success(val routeId: String, val isPublic: Boolean) : PrivacyToggleState()
    data class Error(val message: String) : PrivacyToggleState()
}