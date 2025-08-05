package com.zeynekurtulus.wayfare.data.repository

import com.zeynekurtulus.wayfare.data.api.services.UserApiService
import com.zeynekurtulus.wayfare.data.mappers.UserMapper
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.UserRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkUtils
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager

class UserRepositoryImpl(
    private val userApiService: UserApiService,
    private val sharedPreferencesManager: SharedPreferencesManager
) : UserRepository {
    
    override suspend fun register(userRegistration: UserRegistration): ApiResult<String> {
        return try {
            val request = UserMapper.mapToRegisterRequest(userRegistration)
            val response = userApiService.register(request)
            
            if (response.isSuccessful) {
                response.body()?.let { registerResponse ->
                    // Save username and set user as logged in after successful registration
                    sharedPreferencesManager.saveUsername(userRegistration.username)
                    sharedPreferencesManager.setLoggedIn(true)
                    
                    ApiResult.Success(registerResponse.userId)
                } ?: ApiResult.Error("Registration failed")
            } else {
                ApiResult.Error("Registration failed", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun login(userLogin: UserLogin): ApiResult<AuthTokens> {
        return try {
            val request = UserMapper.mapToLoginRequest(userLogin)
            val response = userApiService.login(request)
            
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    val authTokens = UserMapper.mapToAuthTokens(loginResponse)
                    
                    // Store access token and login state
                    sharedPreferencesManager.saveAccessToken(authTokens.accessToken)
                    sharedPreferencesManager.saveUsername(userLogin.username)
                    sharedPreferencesManager.setLoggedIn(true)
                    
                    ApiResult.Success(authTokens)
                } ?: ApiResult.Error("Wrong username or password")
            } else {
                ApiResult.Error("Invalid credentials", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun addUserInfo(preferences: UserPreferences, homeCity: String): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = UserMapper.mapToAddInfoRequest(preferences, homeCity)
            val response = userApiService.addInfo("Bearer $token", request)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to update user info", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getCurrentUser(): ApiResult<User> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = userApiService.getCurrentUser("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val user = UserMapper.mapToUser(userResponse)
                    
                    // Update stored user info
                    sharedPreferencesManager.saveUserId(user.userId)
                    sharedPreferencesManager.saveEmail(user.email)
                    
                    ApiResult.Success(user)
                } ?: ApiResult.Error("Failed to get user info")
            } else {
                ApiResult.Error("Failed to get user info", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun changePassword(passwordChange: PasswordChange): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = UserMapper.mapToChangePasswordRequest(passwordChange)
            val response = userApiService.changePassword("Bearer $token", request)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to change password", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun deleteUser(password: String): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = UserMapper.mapToDeleteUserRequest(password)
            val response = userApiService.deleteUser("Bearer $token", request)
            
            if (response.isSuccessful) {
                // Clear user session after successful deletion
                sharedPreferencesManager.clearUserSession()
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to delete user", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun sendVerification(email: String): ApiResult<Unit> {
        return try {
            val request = UserMapper.mapToSendVerificationRequest(email)
            val response = userApiService.sendVerification(request)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to send verification code", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun verifyCode(code: String): ApiResult<Unit> {
        return try {
            val request = UserMapper.mapToVerifyCodeRequest(code)
            val response = userApiService.verifyCode(request)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Invalid verification code", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun verifyOtp(email: String, otpCode: String): ApiResult<String> {
        return try {
            val request = UserMapper.mapToVerifyCodeRequest(otpCode)
            val response = userApiService.verifyCode(request)
            
            if (response.isSuccessful) {
                response.body()?.let { verificationResponse ->
                    if (verificationResponse.success) {
                        ApiResult.Success(verificationResponse.message)
                    } else {
                        ApiResult.Error(verificationResponse.message, verificationResponse.statusCode)
                    }
                } ?: ApiResult.Error("Verification failed")
            } else {
                ApiResult.Error("Verification failed", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun resendOtp(email: String): ApiResult<String> {
        return try {
            val request = UserMapper.mapToSendVerificationRequest(email)
            val response = userApiService.sendVerification(request)
            
            if (response.isSuccessful) {
                response.body()?.let { verificationResponse ->
                    if (verificationResponse.success) {
                        ApiResult.Success(verificationResponse.message)
                    } else {
                        ApiResult.Error(verificationResponse.message, verificationResponse.statusCode)
                    }
                } ?: ApiResult.Error("Failed to send OTP")
            } else {
                ApiResult.Error("Failed to send OTP", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override fun isLoggedIn(): Boolean {
        return sharedPreferencesManager.isLoggedIn()
    }
    
    override fun logout() {
        sharedPreferencesManager.clearUserSession()
    }
    
    override fun getStoredUserId(): String? {
        return sharedPreferencesManager.getUserId()
    }
    
    override fun getStoredUsername(): String? {
        return sharedPreferencesManager.getUsername()
    }
    
    override fun getStoredEmail(): String? {
        return sharedPreferencesManager.getEmail()
    }
}