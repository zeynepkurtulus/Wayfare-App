package com.zeynekurtulus.wayfare.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.UserRegistration
import com.zeynekurtulus.wayfare.domain.repository.UserRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.ValidationUtils
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState
    
    private val _verificationState = MutableLiveData<VerificationState>()
    val verificationState: LiveData<VerificationState> = _verificationState
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _formErrors = MutableLiveData<FormErrors>()
    val formErrors: LiveData<FormErrors> = _formErrors
    
    fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        // Clear previous errors
        _formErrors.value = FormErrors()
        
        // Validate input
        val validation = ValidationUtils.validateRegistration(
            username, email, password, firstName, lastName
        )
        
        if (!validation.isValid) {
            handleValidationError(validation.errorMessage ?: "Invalid input", 
                username, email, password, firstName, lastName)
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val userRegistration = UserRegistration(username, email, password, firstName, lastName)
            when (val result = userRepository.register(userRegistration)) {
                is ApiResult.Success -> {
                    _registerState.value = RegisterState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _registerState.value = RegisterState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun sendVerification(email: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _verificationState.value = VerificationState.Error("Please enter a valid email address")
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = userRepository.sendVerification(email)) {
                is ApiResult.Success -> {
                    _verificationState.value = VerificationState.CodeSent
                }
                is ApiResult.Error -> {
                    _verificationState.value = VerificationState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun verifyCode(code: String) {
        if (!ValidationUtils.isValidVerificationCode(code)) {
            _verificationState.value = VerificationState.Error("Please enter a valid 6-digit code")
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = userRepository.verifyCode(code)) {
                is ApiResult.Success -> {
                    _verificationState.value = VerificationState.Verified
                }
                is ApiResult.Error -> {
                    _verificationState.value = VerificationState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    private fun handleValidationError(
        message: String,
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        val errors = FormErrors(
            usernameError = if (!ValidationUtils.isValidUsername(username)) message else null,
            emailError = if (!ValidationUtils.isValidEmail(email)) message else null,
            passwordError = if (!ValidationUtils.isValidPassword(password)) message else null,
            firstNameError = if (!ValidationUtils.isValidName(firstName)) message else null,
            lastNameError = if (!ValidationUtils.isValidName(lastName)) message else null
        )
        _formErrors.value = errors
    }
    
    fun clearErrors() {
        _formErrors.value = FormErrors()
        _registerState.value = RegisterState.Idle
        _verificationState.value = VerificationState.Idle
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    data class Success(val userId: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

sealed class VerificationState {
    object Idle : VerificationState()
    object CodeSent : VerificationState()
    object Verified : VerificationState()
    data class Error(val message: String) : VerificationState()
}

data class FormErrors(
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null
)