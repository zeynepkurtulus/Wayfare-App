package com.zeynekurtulus.wayfare.presentation.activities

/**
 * Destination data class for UI display
 * 
 * This is a simple data class used for displaying destination information
 * in the RecyclerView cards within the HomeFragment.
 */
data class Destination(
    val name: String,
    val imageUrl: String,
    val rating: Float
)