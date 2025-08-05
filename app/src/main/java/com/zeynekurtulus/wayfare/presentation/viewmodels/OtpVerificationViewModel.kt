package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.repository.UserRepository
import com.zeynekurtulus.wayfare.domain.model.UserRegistration
import com.zeynekurtulus.wayfare.utils.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * OtpVerificationViewModel - Handles OTP verification logic
 * 
 * Manages:
 * - OTP code input and validation
 * - Email verification API calls
 * - Resend OTP functionality
 * - Loading and error states
 */
class OtpVerificationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // OTP verification state
    private val _verificationState = MutableLiveData<ApiResult<String>>()
    val verificationState: LiveData<ApiResult<String>> = _verificationState
    
    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // OTP input values (6 digits)
    private val _otpDigits = MutableLiveData(arrayOf("", "", "", "", "", ""))
    val otpDigits: LiveData<Array<String>> = _otpDigits
    
    // Error message for OTP
    private val _otpError = MutableLiveData<String?>()
    val otpError: LiveData<String?> = _otpError
    
    // Resend functionality
    private val _canResend = MutableLiveData(false)
    val canResend: LiveData<Boolean> = _canResend
    
    private val _resendCountdown = MutableLiveData(60) // 60 seconds countdown
    val resendCountdown: LiveData<Int> = _resendCountdown
    
    // User email
    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail
    
    // Store pending registration data (if this is for registration completion)
    private var pendingRegistration: UserRegistration? = null
    
    init {
        startResendCountdown()
    }
    
    /**
     * Set the user email for OTP verification
     */
    fun setUserEmail(email: String) {
        _userEmail.value = email
    }
    
    /**
     * Set pending registration data for completion after OTP verification
     */
    fun setPendingRegistration(userRegistration: UserRegistration?) {
        pendingRegistration = userRegistration
    }
    
    /**
     * Update OTP digit at specific position
     */
    fun updateOtpDigit(position: Int, digit: String) {
        if (position in 0..5 && digit.length <= 1 && (digit.isEmpty() || digit.all { it.isDigit() })) {
            val currentDigits = _otpDigits.value ?: arrayOf("", "", "", "", "", "")
            currentDigits[position] = digit
            _otpDigits.value = currentDigits
            
            // Clear error when user starts typing
            if (digit.isNotEmpty()) {
                _otpError.value = null
            }
        }
    }
    
    /**
     * Get complete OTP code
     */
    private fun getCompleteOtpCode(): String {
        return _otpDigits.value?.joinToString("") ?: ""
    }
    
    /**
     * Validate OTP code
     */
    private fun validateOtp(): Boolean {
        val otpCode = getCompleteOtpCode()
        
        if (otpCode.length != 6) {
            _otpError.value = "Please enter complete 6-digit OTP code"
            return false
        }
        
        if (!otpCode.all { it.isDigit() }) {
            _otpError.value = "OTP code should contain only numbers"
            return false
        }
        
        _otpError.value = null
        return true
    }
    
    /**
     * Verify OTP with backend
     */
    fun verifyOtp() {
        if (!validateOtp()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val email = _userEmail.value ?: ""
                val otpCode = getCompleteOtpCode()
                
                // Call the actual API for OTP verification
                val result = userRepository.verifyOtp(email, otpCode)
                
                // If OTP verification is successful and we have pending registration, complete it
                if (result is ApiResult.Success && pendingRegistration != null) {
                    completeRegistrationAfterVerification()
                } else {
                    _verificationState.value = result
                }
                
            } catch (e: Exception) {
                _verificationState.value = ApiResult.Error("Verification failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Resend OTP code
     */
    fun resendOtp() {
        if (_canResend.value != true) return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val email = _userEmail.value ?: ""
                
                // Call the actual API for resending OTP
                val result = userRepository.resendOtp(email)
                _verificationState.value = result
                
                // Reset countdown only if successful
                if (result is ApiResult.Success) {
                    startResendCountdown()
                }
                
            } catch (e: Exception) {
                _verificationState.value = ApiResult.Error("Failed to resend OTP: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Start countdown timer for resend functionality
     */
    private fun startResendCountdown() {
        _canResend.value = false
        _resendCountdown.value = 60
        
        viewModelScope.launch {
            repeat(60) {
                delay(1000) // Wait 1 second
                val currentCount = _resendCountdown.value ?: 0
                if (currentCount > 0) {
                    _resendCountdown.value = currentCount - 1
                }
            }
            _canResend.value = true
        }
    }
    
    /**
     * Clear all OTP digits
     */
    fun clearOtp() {
        _otpDigits.value = arrayOf("", "", "", "", "", "")
        _otpError.value = null
    }
    
    /**
     * Reset verification state
     */
    fun resetVerificationState() {
        _verificationState.value = ApiResult.Loading
    }
    
    /**
     * Complete user registration after successful OTP verification
     */
    private suspend fun completeRegistrationAfterVerification() {
        try {
            val registration = pendingRegistration ?: return
            
            when (val registrationResult = userRepository.register(registration)) {
                is ApiResult.Success -> {
                    // Registration completed successfully
                    pendingRegistration = null
                    _verificationState.value = ApiResult.Success("Registration completed successfully! Welcome to Wayfare!")
                }
                is ApiResult.Error -> {
                    _verificationState.value = ApiResult.Error("Email verified, but registration failed: ${registrationResult.message}")
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
        } catch (e: Exception) {
            _verificationState.value = ApiResult.Error("Registration completion failed: ${e.message}")
        }
    }
}