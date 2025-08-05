package com.zeynekurtulus.wayfare.data.mappers

import com.zeynekurtulus.wayfare.data.api.dto.user.*
import com.zeynekurtulus.wayfare.domain.model.*

object UserMapper {
    
    fun mapToRegisterRequest(userRegistration: UserRegistration): RegisterRequest {
        return RegisterRequest(
            username = userRegistration.username,
            password = userRegistration.password,
            email = userRegistration.email,
            firstName = userRegistration.firstName,
            lastName = userRegistration.lastName
        )
    }
    
    fun mapToLoginRequest(userLogin: UserLogin): LoginRequest {
        return LoginRequest(
            username = userLogin.username,
            password = userLogin.password
        )
    }
    
    fun mapToAddInfoRequest(preferences: UserPreferences, homeCity: String): AddInfoRequest {
        return AddInfoRequest(
            preferences = mapToUserPreferencesDto(preferences),
            homeCity = homeCity
        )
    }
    
    fun mapToChangePasswordRequest(passwordChange: PasswordChange): ChangePasswordRequest {
        return ChangePasswordRequest(
            currentPassword = passwordChange.currentPassword,
            newPassword = passwordChange.newPassword,
            confirmPassword = passwordChange.confirmPassword
        )
    }
    
    fun mapToDeleteUserRequest(password: String): DeleteUserRequest {
        return DeleteUserRequest(password = password)
    }
    
    fun mapToSendVerificationRequest(email: String): SendVerificationRequest {
        return SendVerificationRequest(email = email)
    }
    
    fun mapToVerifyCodeRequest(code: String): VerifyCodeRequest {
        return VerifyCodeRequest(verificationCode = code)
    }
    
    fun mapToAuthTokens(loginResponse: LoginResponse): AuthTokens {
        return AuthTokens(accessToken = loginResponse.accessToken)
    }
    
    fun mapToUser(userResponse: GetCurrentUserResponse): User {
        return User(
            userId = userResponse.userId,
            username = userResponse.username,
            email = userResponse.email,
            firstName = userResponse.name,
            lastName = userResponse.surname,
            preferences = userResponse.preferences?.let { mapToUserPreferences(it) },
            homeCity = userResponse.homeCity
        )
    }
    
    private fun mapToUserPreferencesDto(userPreferences: UserPreferences): UserPreferencesDto {
        return UserPreferencesDto(
            interests = userPreferences.interests,
            budget = userPreferences.budget,
            travelStyle = userPreferences.travelStyle
        )
    }
    
    private fun mapToUserPreferences(userPreferencesDto: UserPreferencesDto): UserPreferences {
        return UserPreferences(
            interests = userPreferencesDto.interests,
            budget = userPreferencesDto.budget,
            travelStyle = userPreferencesDto.travelStyle
        )
    }
}