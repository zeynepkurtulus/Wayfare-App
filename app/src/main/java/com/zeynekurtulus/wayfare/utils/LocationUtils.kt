package com.zeynekurtulus.wayfare.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

object LocationUtils {
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if GPS is enabled
     */
    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    /**
     * Calculate distance between two coordinates in kilometers
     */
    fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0] / 1000 // Convert meters to kilometers
    }
    
    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: Float): String {
        return when {
            distanceKm < 1 -> "${(distanceKm * 1000).toInt()} m"
            distanceKm < 10 -> String.format("%.1f km", distanceKm)
            else -> "${distanceKm.toInt()} km"
        }
    }
    
    /**
     * Check if coordinates are valid
     */
    fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return ValidationUtils.isValidCoordinates(latitude, longitude)
    }
    
    /**
     * Get city name from coordinates (you can implement reverse geocoding here)
     */
    fun getCityFromCoordinates(latitude: Double, longitude: Double): String? {
        // TODO: Implement reverse geocoding using Geocoder or API
        // For now, return null - this can be implemented later
        return null
    }
}

/**
 * Location permission request callback
 */
interface LocationPermissionCallback {
    fun onPermissionGranted()
    fun onPermissionDenied()
}

/**
 * Helper class for requesting location permissions in Activities
 */
class LocationPermissionHelper(
    private val activity: FragmentActivity,
    private val callback: LocationPermissionCallback
) {
    
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted && coarseLocationGranted) {
            callback.onPermissionGranted()
        } else {
            callback.onPermissionDenied()
        }
    }
    
    fun requestPermissions() {
        if (LocationUtils.hasLocationPermissions(activity)) {
            callback.onPermissionGranted()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

/**
 * Helper class for requesting location permissions in Fragments
 */
class FragmentLocationPermissionHelper(
    private val fragment: Fragment,
    private val callback: LocationPermissionCallback
) {
    
    private val requestPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted && coarseLocationGranted) {
            callback.onPermissionGranted()
        } else {
            callback.onPermissionDenied()
        }
    }
    
    fun requestPermissions() {
        if (LocationUtils.hasLocationPermissions(fragment.requireContext())) {
            callback.onPermissionGranted()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}