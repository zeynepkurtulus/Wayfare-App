package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.UserRegistration
import com.zeynekurtulus.wayfare.domain.repository.UserRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.ValidationUtils
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _registrationState = MutableLiveData<RegistrationState>()
    val registrationState: LiveData<RegistrationState> = _registrationState
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Store user registration data for later use after OTP verification
    private var pendingUserRegistration: UserRegistration? = null
    
    // Individual field error states
    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError
    
    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError
    
    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError
    
    private val _firstNameError = MutableLiveData<String?>()
    val firstNameError: LiveData<String?> = _firstNameError
    
    private val _lastNameError = MutableLiveData<String?>()
    val lastNameError: LiveData<String?> = _lastNameError
    
    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError: LiveData<String?> = _confirmPasswordError
    
    /**
     * Registers a new user with the provided information
     */
    fun registerUser(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ) {
        // Clear previous errors
        clearErrors()
        
        // Validate input
        val validation = validateRegistrationInput(
            username, email, password, confirmPassword, firstName, lastName
        )
        
        if (!validation.isValid) {
            // Set specific field errors based on validation
            when {
                username.isEmpty() || !ValidationUtils.isValidUsername(username) -> 
                    _usernameError.value = validation.errorMessage
                email.isEmpty() || !ValidationUtils.isValidEmail(email) -> 
                    _emailError.value = validation.errorMessage
                password.isEmpty() || !ValidationUtils.isValidPassword(password) -> 
                    _passwordError.value = validation.errorMessage
                confirmPassword.isEmpty() || password != confirmPassword -> 
                    _confirmPasswordError.value = validation.errorMessage
                firstName.isEmpty() || !ValidationUtils.isValidName(firstName) -> 
                    _firstNameError.value = validation.errorMessage
                lastName.isEmpty() || !ValidationUtils.isValidName(lastName) -> 
                    _lastNameError.value = validation.errorMessage
                else -> _registrationState.value = RegistrationState.Error(validation.errorMessage ?: "Invalid input")
            }
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            val userRegistration = UserRegistration(
                username = username.trim(),
                email = email.trim().lowercase(),
                password = password,
                firstName = firstName.trim(),
                lastName = lastName.trim()
            )
            
            // Store registration data for later use after OTP verification
            pendingUserRegistration = userRegistration
            
            // Send OTP for email verification BEFORE registration
            when (val otpResult = userRepository.resendOtp(userRegistration.email)) {
                is ApiResult.Success -> {
                    // OTP sent successfully, navigate to OTP verification
                    _registrationState.value = RegistrationState.Success("Verification code sent to your email. Please check your inbox.")
                }
                is ApiResult.Error -> {
                    _registrationState.value = RegistrationState.Error(otpResult.message)
                }
                is ApiResult.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }
    
    /**
     * Actually register the user after email verification
     * This should be called from OTP verification activity after successful verification
     */
    suspend fun completeRegistration(): ApiResult<String> {
        val userRegistration = pendingUserRegistration
            ?: return ApiResult.Error("No pending registration found")
        
        return when (val result = userRepository.register(userRegistration)) {
            is ApiResult.Success -> {
                // Clear pending registration
                pendingUserRegistration = null
                ApiResult.Success("Registration completed successfully!")
            }
            is ApiResult.Error -> {
                ApiResult.Error(result.message)
            }
            is ApiResult.Loading -> {
                ApiResult.Loading
            }
        }
    }
    
    /**
     * Get pending user registration data
     */
    fun getPendingUserRegistration(): UserRegistration? = pendingUserRegistration
    
    /**
     * Validates registration input and returns detailed validation result
     */
    private fun validateRegistrationInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ): ValidationUtils.ValidationResult {
        // Check password confirmation first
        if (password != confirmPassword) {
            return ValidationUtils.ValidationResult(false, "Passwords do not match")
        }
        
        // Use existing validation method
        return ValidationUtils.validateRegistration(
            username = username.trim(),
            email = email.trim(),
            password = password,
            firstName = firstName.trim(),
            lastName = lastName.trim()
        )
    }
    
    /**
     * Validates individual fields for real-time feedback
     */
    fun validateUsername(username: String): String? {
        return when {
            username.isEmpty() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username can only contain letters, numbers, and underscores"
            else -> null
        }
    }
    
    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !ValidationUtils.isValidEmail(email) -> "Please enter a valid email address"
            else -> null
        }
    }
    
    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters long"
            else -> null
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
    
    fun validateFirstName(firstName: String): String? {
        return when {
            firstName.isEmpty() -> "First name is required"
            firstName.length < 2 -> "First name must be at least 2 characters"
            !firstName.matches(Regex("^[a-zA-Z\\s]+$")) -> "First name can only contain letters and spaces"
            else -> null
        }
    }
    
    fun validateLastName(lastName: String): String? {
        return when {
            lastName.isEmpty() -> "Last name is required"
            lastName.length < 2 -> "Last name must be at least 2 characters"
            !lastName.matches(Regex("^[a-zA-Z\\s]+$")) -> "Last name can only contain letters and spaces"
            else -> null
        }
    }
    
    /**
     * Checks if username is available (can be extended to check with server)
     */
    fun checkUsernameAvailability(username: String): Boolean {
        // TODO: Implement server-side username availability check
        // For now, just validate format
        return ValidationUtils.isValidUsername(username)
    }
    
    /**
     * Checks if email is available (can be extended to check with server)
     */
    fun checkEmailAvailability(email: String): Boolean {
        // TODO: Implement server-side email availability check
        // For now, just validate format
        return ValidationUtils.isValidEmail(email)
    }
    
    /**
     * Clears all form errors
     */
    fun clearErrors() {
        _usernameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
        _firstNameError.value = null
        _lastNameError.value = null
        _registrationState.value = RegistrationState.Idle
    }
    
    /**
     * Clears specific field error
     */
    fun clearFieldError(field: RegistrationField) {
        when (field) {
            RegistrationField.USERNAME -> _usernameError.value = null
            RegistrationField.EMAIL -> _emailError.value = null
            RegistrationField.PASSWORD -> _passwordError.value = null
            RegistrationField.CONFIRM_PASSWORD -> _confirmPasswordError.value = null
            RegistrationField.FIRST_NAME -> _firstNameError.value = null
            RegistrationField.LAST_NAME -> _lastNameError.value = null
        }
    }
    
    /**
     * Checks if all required fields are filled
     */
    fun areAllFieldsFilled(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ): Boolean {
        return username.isNotEmpty() &&
                email.isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                firstName.isNotEmpty() &&
                lastName.isNotEmpty()
    }
    
    /**
     * Returns current loading state
     */
    fun getCurrentLoadingState(): Boolean {
        return _isLoading.value ?: false
    }
}

/**
 * Sealed class representing different registration states
 */
sealed class RegistrationState {
    object Idle : RegistrationState()
    data class Success(val userId: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

/**
 * Enum for registration form fields
 */
enum class RegistrationField {
    USERNAME,
    EMAIL,
    PASSWORD,
    CONFIRM_PASSWORD,
    FIRST_NAME,
    LAST_NAME
}