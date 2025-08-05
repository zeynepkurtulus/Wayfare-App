package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.utils.ApiResult

interface UserRepository {
    
    suspend fun register(userRegistration: UserRegistration): ApiResult<String>
    
    suspend fun login(userLogin: UserLogin): ApiResult<AuthTokens>
    
    suspend fun addUserInfo(preferences: UserPreferences, homeCity: String): ApiResult<Unit>
    
    suspend fun getCurrentUser(): ApiResult<User>
    
    suspend fun changePassword(passwordChange: PasswordChange): ApiResult<Unit>
    
    suspend fun deleteUser(password: String): ApiResult<Unit>
    
    suspend fun sendVerification(email: String): ApiResult<Unit>
    
    suspend fun verifyCode(code: String): ApiResult<Unit>
    
    suspend fun verifyOtp(email: String, otpCode: String): ApiResult<String>
    
    suspend fun resendOtp(email: String): ApiResult<String>
    
    fun isLoggedIn(): Boolean
    
    fun logout()
    
    fun getStoredUserId(): String?
    
    fun getStoredUsername(): String?
    
    fun getStoredEmail(): String?
}