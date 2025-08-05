package com.zeynekurtulus.wayfare.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val preferences: UserPreferences?,
    val homeCity: String?
) : Parcelable

@Parcelize
data class UserPreferences(
    val interests: List<String>,
    val budget: String,
    val travelStyle: String
) : Parcelable

data class AuthTokens(
    val accessToken: String
)

@Parcelize
data class UserRegistration(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
) : Parcelable

data class UserLogin(
    val username: String,
    val password: String
)

data class PasswordChange(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class VerificationCode(
    val email: String,
    val code: String
)