package com.zeynekurtulus.wayfare.utils

import android.util.Patterns

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
    
    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
    
    fun isValidName(name: String): Boolean {
        return name.isNotEmpty() && name.length >= 2 && name.matches(Regex("^[a-zA-Z\\s]+$"))
    }
    
    fun isValidVerificationCode(code: String): Boolean {
        return code.length == Constants.VERIFICATION_CODE_LENGTH && code.matches(Regex("^[0-9]+$"))
    }
    
    fun isValidRating(rating: Int): Boolean {
        return rating in Constants.MIN_RATING..Constants.MAX_RATING
    }
    
    fun isValidComment(comment: String): Boolean {
        return comment.length <= 1000 // API limit for comments
    }
    
    fun isValidRouteTitle(title: String): Boolean {
        return title.isNotEmpty() && title.length <= 100
    }
    
    fun isValidCityName(city: String): Boolean {
        return city.isNotEmpty() && city.matches(Regex("^[a-zA-Z\\s]+$"))
    }
    
    fun isValidPlaceName(placeName: String): Boolean {
        return placeName.isNotEmpty() && placeName.length <= 200
    }
    
    fun isValidNotes(notes: String): Boolean {
        return notes.length <= 500
    }
    
    fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
    
    fun isValidBudget(budget: String): Boolean {
        return budget in listOf(Constants.Budget.LOW, Constants.Budget.MEDIUM, Constants.Budget.HIGH)
    }
    
    fun isValidTravelStyle(travelStyle: String): Boolean {
        return travelStyle in listOf(
            Constants.TravelStyle.RELAXED,
            Constants.TravelStyle.MODERATE,
            Constants.TravelStyle.ACCELERATED
        )
    }
    
    fun isValidCategory(category: String): Boolean {
        return category in listOf(
            Constants.Category.CITY_BREAK,
            Constants.Category.BEACH,
            Constants.Category.MOUNTAIN,
            Constants.Category.ROAD_TRIP
        )
    }
    
    fun isValidSeason(season: String): Boolean {
        return season in listOf(
            Constants.Season.SPRING,
            Constants.Season.SUMMER,
            Constants.Season.AUTUMN,
            Constants.Season.WINTER
        )
    }
    
    fun isValidDateRange(startDate: String, endDate: String): Boolean {
        return DateUtils.isValidDateRange(startDate, endDate)
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    fun validateRegistration(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): ValidationResult {
        return when {
            !isValidUsername(username) -> ValidationResult(false, "Username must be at least 3 characters and contain only letters, numbers, and underscores")
            !isValidEmail(email) -> ValidationResult(false, "Please enter a valid email address")
            !isValidPassword(password) -> ValidationResult(false, "Password must be at least 8 characters long")
            !isValidName(firstName) -> ValidationResult(false, "Please enter a valid first name")
            !isValidName(lastName) -> ValidationResult(false, "Please enter a valid last name")
            else -> ValidationResult(true)
        }
    }
    
    fun validateLogin(username: String, password: String): ValidationResult {
        return when {
            username.isEmpty() -> ValidationResult(false, "Please enter your username")
            password.isEmpty() -> ValidationResult(false, "Please enter your password")
            else -> ValidationResult(true)
        }
    }
    
    fun validatePasswordChange(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): ValidationResult {
        return when {
            currentPassword.isEmpty() -> ValidationResult(false, "Please enter your current password")
            !isValidPassword(newPassword) -> ValidationResult(false, "New password must be at least 8 characters long")
            newPassword != confirmPassword -> ValidationResult(false, "Passwords do not match")
            currentPassword == newPassword -> ValidationResult(false, "New password must be different from current password")
            else -> ValidationResult(true)
        }
    }
}