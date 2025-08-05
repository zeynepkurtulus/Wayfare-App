package com.zeynekurtulus.wayfare.data.api.dto.user

import com.google.gson.annotations.SerializedName

// Request DTOs
data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
)

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class AddInfoRequest(
    @SerializedName("preferences") val preferences: UserPreferencesDto,
    @SerializedName("home_city") val homeCity: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

data class DeleteUserRequest(
    @SerializedName("password") val password: String
)

data class SendVerificationRequest(
    @SerializedName("email") val email: String
)

data class VerifyCodeRequest(
    @SerializedName("verification_code") val verificationCode: String
)

// Response DTOs
data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("user_id") val userId: String
)

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("access_token") val accessToken: String
)

data class GetCurrentUserResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("preferences") val preferences: UserPreferencesDto?,
    @SerializedName("home_city") val homeCity: String?
)

data class UserPreferencesDto(
    @SerializedName("interests") val interests: List<String>,
    @SerializedName("budget") val budget: String,
    @SerializedName("travel_style") val travelStyle: String
)

data class ApiSuccessResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("status_code") val statusCode: Int
)

data class VerificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int
)

data class ApiErrorResponse(
    @SerializedName("detail") val detail: String,
    @SerializedName("success") val success: Boolean? = false,
    @SerializedName("status_code") val statusCode: Int? = null
)