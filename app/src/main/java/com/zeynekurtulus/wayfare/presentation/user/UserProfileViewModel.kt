package com.zeynekurtulus.wayfare.presentation.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.PasswordChange
import com.zeynekurtulus.wayfare.domain.model.User
import com.zeynekurtulus.wayfare.domain.model.UserPreferences
import com.zeynekurtulus.wayfare.domain.repository.UserRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.ValidationUtils
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _userInfoState = MutableLiveData<UserInfoState>()
    val userInfoState: LiveData<UserInfoState> = _userInfoState
    
    private val _passwordChangeState = MutableLiveData<PasswordChangeState>()
    val passwordChangeState: LiveData<PasswordChangeState> = _passwordChangeState
    
    private val _deleteAccountState = MutableLiveData<DeleteAccountState>()
    val deleteAccountState: LiveData<DeleteAccountState> = _deleteAccountState
    
    init {
        loadCurrentUser()
    }
    
    fun loadCurrentUser() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is ApiResult.Success -> {
                    _user.value = result.data
                }
                is ApiResult.Error -> {
                    _userInfoState.value = UserInfoState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun updateUserInfo(preferences: UserPreferences, homeCity: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = userRepository.addUserInfo(preferences, homeCity)) {
                is ApiResult.Success -> {
                    _userInfoState.value = UserInfoState.Updated
                    loadCurrentUser() // Reload user data
                }
                is ApiResult.Error -> {
                    _userInfoState.value = UserInfoState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val validation = ValidationUtils.validatePasswordChange(
            currentPassword, newPassword, confirmPassword
        )
        
        if (!validation.isValid) {
            _passwordChangeState.value = PasswordChangeState.Error(
                validation.errorMessage ?: "Invalid input"
            )
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val passwordChange = PasswordChange(currentPassword, newPassword, confirmPassword)
            when (val result = userRepository.changePassword(passwordChange)) {
                is ApiResult.Success -> {
                    _passwordChangeState.value = PasswordChangeState.Success
                }
                is ApiResult.Error -> {
                    _passwordChangeState.value = PasswordChangeState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun deleteAccount(password: String) {
        if (password.isEmpty()) {
            _deleteAccountState.value = DeleteAccountState.Error("Password is required")
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = userRepository.deleteUser(password)) {
                is ApiResult.Success -> {
                    _deleteAccountState.value = DeleteAccountState.Success
                }
                is ApiResult.Error -> {
                    _deleteAccountState.value = DeleteAccountState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    fun logout() {
        userRepository.logout()
    }
    
    fun getStoredUsername(): String? {
        return userRepository.getStoredUsername()
    }
    
    fun getStoredEmail(): String? {
        return userRepository.getStoredEmail()
    }
    
    fun clearStates() {
        _userInfoState.value = UserInfoState.Idle
        _passwordChangeState.value = PasswordChangeState.Idle
        _deleteAccountState.value = DeleteAccountState.Idle
    }
}

sealed class UserInfoState {
    object Idle : UserInfoState()
    object Updated : UserInfoState()
    data class Error(val message: String) : UserInfoState()
}

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}

sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Success : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}