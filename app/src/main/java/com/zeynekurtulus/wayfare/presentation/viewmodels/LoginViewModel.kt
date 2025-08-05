package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.AuthTokens
import com.zeynekurtulus.wayfare.domain.model.UserLogin
import com.zeynekurtulus.wayfare.domain.repository.UserRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.ValidationUtils
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError
    
    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError
    
    fun login(username: String, password: String) {
        // Clear previous errors
        _usernameError.value = null
        _passwordError.value = null
        
        // Validate input
        val validation = ValidationUtils.validateLogin(username, password)
        if (!validation.isValid) {
            when {
                username.isEmpty() -> _usernameError.value = validation.errorMessage
                password.isEmpty() -> _passwordError.value = validation.errorMessage
                else -> _loginState.value = LoginState.Error(validation.errorMessage ?: "Invalid input")
            }
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val userLogin = UserLogin(username, password)
            when (val result = userRepository.login(userLogin)) {
                is ApiResult.Success -> {
                    _loginState.value = LoginState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun clearErrors() {
        _usernameError.value = null
        _passwordError.value = null
        _loginState.value = LoginState.Idle
    }
    
    fun isUserLoggedIn(): Boolean {
        return userRepository.isLoggedIn()
    }
}

sealed class LoginState {
    object Idle : LoginState()
    data class Success(val tokens: AuthTokens) : LoginState()
    data class Error(val message: String) : LoginState()
}