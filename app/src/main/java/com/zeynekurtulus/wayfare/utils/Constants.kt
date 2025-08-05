package com.zeynekurtulus.wayfare.utils

object Constants {
    
    // API Configuration
    // Development: http://localhost:8000/
    // Production: https://api.wayfare.com/
    const val BASE_URL = "http://10.0.2.2:8000/"  // For Android Emulator
    const val API_TIMEOUT = 30L // seconds
    
    // SharedPreferences Keys
    const val PREF_NAME = "wayfare_preferences"
    const val PREF_ACCESS_TOKEN = "access_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_EMAIL = "email"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    
    // Request Codes
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    const val CAMERA_PERMISSION_REQUEST_CODE = 1002
    
    // Location Settings
    const val DEFAULT_LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
    const val FASTEST_LOCATION_UPDATE_INTERVAL = 5000L  // 5 seconds
    const val LOCATION_REQUEST_DISPLACEMENT = 10f       // 10 meters
    
    // Bundle Keys
    const val BUNDLE_ROUTE_ID = "route_id"
    const val BUNDLE_PLACE_ID = "place_id"
    const val BUNDLE_CITY_NAME = "city_name"
    const val BUNDLE_COUNTRY_NAME = "country_name"
    
    // Travel Preferences
    object TravelStyle {
        const val RELAXED = "relaxed"
        const val MODERATE = "moderate"
        const val ACCELERATED = "accelerated"
    }
    
    object Budget {
        const val LOW = "low"
        const val MEDIUM = "medium"
        const val HIGH = "high"
    }
    
    object Category {
        const val CITY_BREAK = "city_break"
        const val BEACH = "beach"
        const val MOUNTAIN = "mountain"
        const val ROAD_TRIP = "road_trip"
    }
    
    object Season {
        const val SPRING = "spring"
        const val SUMMER = "summer"
        const val AUTUMN = "autumn"
        const val WINTER = "winter"
    }
    
    object Interests {
        const val MUSEUMS = "Museums and Art Galleries"
        const val FOOD_DRINKS = "Food & Drinks"
        const val OUTDOORS = "Outdoors"
        const val HIDDEN_GEMS = "Hidden Gems"
        const val FAMILY_FRIENDLY = "Family Friendly"
        const val ARCHITECTURE = "architecture"
        const val NIGHTLIFE = "nightlife"
        const val SHOPPING = "shopping"
        const val HISTORICAL = "historical"
        const val NATURE = "nature"
    }
    
    // Error Messages
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_UNAUTHORIZED = "Session expired. Please log in again."
    const val ERROR_SERVER = "Server error. Please try again later."
    const val ERROR_UNKNOWN = "An unexpected error occurred."
    const val ERROR_VALIDATION = "Please check your input and try again."
    
    // Date Formats
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    const val TIME_FORMAT_API = "HH:mm"
    const val TIME_FORMAT_DISPLAY = "h:mm a"
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 10
    const val DEFAULT_LIMIT = 20
    
    // Cache Duration (in milliseconds)
    const val CACHE_DURATION_CITIES = 24 * 60 * 60 * 1000L // 24 hours
    const val CACHE_DURATION_PLACES = 12 * 60 * 60 * 1000L // 12 hours
    
    // Map Configuration
    const val DEFAULT_MAP_ZOOM = 15f
    const val DEFAULT_CITY_ZOOM = 12f
    
    // Rating Configuration
    const val MIN_RATING = 1
    const val MAX_RATING = 5
    
    // Verification Code
    const val VERIFICATION_CODE_LENGTH = 6
    const val VERIFICATION_CODE_EXPIRY_MINUTES = 10
}