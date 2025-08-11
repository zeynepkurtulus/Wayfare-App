package com.zeynekurtulus.wayfare.presentation.activities

/**
 * Trip data class for UI display
 * 
 * This is a simple data class used for displaying trip information
 * in the RecyclerView cards within the HomeFragment.
 */
data class Trip(
    val name: String,
    val imageUrl: String,
    val isPublic: Boolean = false,  // Privacy indicator for UI
    val routeId: String = ""  // Route ID for download functionality
)