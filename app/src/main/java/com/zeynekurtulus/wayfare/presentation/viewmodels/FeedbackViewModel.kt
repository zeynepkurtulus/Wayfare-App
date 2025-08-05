package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.FeedbackRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.ValidationUtils
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {
    
    // Place Feedback
    private val _placeFeedback = MutableLiveData<List<PlaceFeedback>>()
    val placeFeedback: LiveData<List<PlaceFeedback>> = _placeFeedback
    
    private val _placeFeedbackStats = MutableLiveData<FeedbackStats?>()
    val placeFeedbackStats: LiveData<FeedbackStats?> = _placeFeedbackStats
    
    // Route Feedback
    private val _routeFeedback = MutableLiveData<List<RouteFeedback>>()
    val routeFeedback: LiveData<List<RouteFeedback>> = _routeFeedback
    
    private val _routeFeedbackStats = MutableLiveData<FeedbackStats?>()
    val routeFeedbackStats: LiveData<FeedbackStats?> = _routeFeedbackStats
    
    // UI States
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _submitState = MutableLiveData<SubmitFeedbackState>()
    val submitState: LiveData<SubmitFeedbackState> = _submitState
    
    private val _updateState = MutableLiveData<UpdateFeedbackState>()
    val updateState: LiveData<UpdateFeedbackState> = _updateState
    
    private val _deleteState = MutableLiveData<DeleteFeedbackState>()
    val deleteState: LiveData<DeleteFeedbackState> = _deleteState
    
    // Form validation
    private val _formErrors = MutableLiveData<FeedbackFormErrors>()
    val formErrors: LiveData<FeedbackFormErrors> = _formErrors
    
    // Place Feedback Methods
    fun submitPlaceFeedback(
        placeId: String,
        rating: Int,
        comment: String?,
        visitedOn: String
    ) {
        val validation = validateFeedbackInput(rating, comment)
        if (!validation.isValid) {
            _formErrors.value = validation.errors
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val createFeedback = CreatePlaceFeedback(placeId, rating, comment, visitedOn)
            when (val result = feedbackRepository.submitPlaceFeedback(createFeedback)) {
                is ApiResult.Success -> {
                    _submitState.value = SubmitFeedbackState.Success(result.data)
                    // Reload feedback for this place
                    getPlaceFeedback(placeId)
                }
                is ApiResult.Error -> {
                    _submitState.value = SubmitFeedbackState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun getPlaceFeedback(placeId: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = feedbackRepository.getPlaceFeedback(placeId)) {
                is ApiResult.Success -> {
                    _placeFeedback.value = result.data
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
    
    fun getPlaceFeedbackStats(placeId: String) {
        viewModelScope.launch {
            when (val result = feedbackRepository.getPlaceFeedbackStats(placeId)) {
                is ApiResult.Success -> {
                    _placeFeedbackStats.value = result.data
                }
                is ApiResult.Error -> {
                    // Don't show error for stats failures
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }
    
    fun updatePlaceFeedback(
        feedbackId: String,
        rating: Int?,
        comment: String?,
        visitedOn: String?
    ) {
        if (rating != null) {
            val validation = validateFeedbackInput(rating, comment)
            if (!validation.isValid) {
                _formErrors.value = validation.errors
                return
            }
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val updateFeedback = UpdatePlaceFeedback(rating, comment, visitedOn)
            when (val result = feedbackRepository.updatePlaceFeedback(feedbackId, updateFeedback)) {
                is ApiResult.Success -> {
                    _updateState.value = UpdateFeedbackState.Success
                }
                is ApiResult.Error -> {
                    _updateState.value = UpdateFeedbackState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    // Route Feedback Methods
    fun submitRouteFeedback(
        routeId: String,
        rating: Int,
        comment: String?,
        visitedOn: String
    ) {
        val validation = validateFeedbackInput(rating, comment)
        if (!validation.isValid) {
            _formErrors.value = validation.errors
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val createFeedback = CreateRouteFeedback(routeId, rating, comment, visitedOn)
            when (val result = feedbackRepository.submitRouteFeedback(createFeedback)) {
                is ApiResult.Success -> {
                    _submitState.value = SubmitFeedbackState.Success(result.data)
                    // Reload feedback for this route
                    getRouteFeedback(routeId)
                }
                is ApiResult.Error -> {
                    _submitState.value = SubmitFeedbackState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun getRouteFeedback(routeId: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            when (val result = feedbackRepository.getRouteFeedback(routeId)) {
                is ApiResult.Success -> {
                    _routeFeedback.value = result.data
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
    
    fun getRouteFeedbackStats(routeId: String) {
        viewModelScope.launch {
            when (val result = feedbackRepository.getRouteFeedbackStats(routeId)) {
                is ApiResult.Success -> {
                    _routeFeedbackStats.value = result.data
                }
                is ApiResult.Error -> {
                    // Don't show error for stats failures
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }
    
    fun deleteFeedback(feedbackId: String, isPlaceFeedback: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = if (isPlaceFeedback) {
                feedbackRepository.deletePlaceFeedback(feedbackId)
            } else {
                feedbackRepository.deleteRouteFeedback(feedbackId)
            }
            
            when (result) {
                is ApiResult.Success -> {
                    _deleteState.value = DeleteFeedbackState.Success(feedbackId)
                }
                is ApiResult.Error -> {
                    _deleteState.value = DeleteFeedbackState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    private fun validateFeedbackInput(rating: Int, comment: String?): FeedbackValidationResult {
        val errors = FeedbackFormErrors()
        var isValid = true
        
        if (!ValidationUtils.isValidRating(rating)) {
            errors.ratingError = "Please select a rating between 1 and 5"
            isValid = false
        }
        
        if (comment != null && !ValidationUtils.isValidComment(comment)) {
            errors.commentError = "Comment must be less than 1000 characters"
            isValid = false
        }
        
        return FeedbackValidationResult(isValid, errors)
    }
    
    fun clearStates() {
        _submitState.value = SubmitFeedbackState.Idle
        _updateState.value = UpdateFeedbackState.Idle
        _deleteState.value = DeleteFeedbackState.Idle
        _formErrors.value = FeedbackFormErrors()
        _error.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
}

// States
sealed class SubmitFeedbackState {
    object Idle : SubmitFeedbackState()
    data class Success(val feedbackId: String) : SubmitFeedbackState()
    data class Error(val message: String) : SubmitFeedbackState()
}

sealed class UpdateFeedbackState {
    object Idle : UpdateFeedbackState()
    object Success : UpdateFeedbackState()
    data class Error(val message: String) : UpdateFeedbackState()
}

sealed class DeleteFeedbackState {
    object Idle : DeleteFeedbackState()
    data class Success(val feedbackId: String) : DeleteFeedbackState()
    data class Error(val message: String) : DeleteFeedbackState()
}

data class FeedbackFormErrors(
    var ratingError: String? = null,
    var commentError: String? = null
)

data class FeedbackValidationResult(
    val isValid: Boolean,
    val errors: FeedbackFormErrors
)