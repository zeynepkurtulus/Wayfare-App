package com.zeynekurtulus.wayfare.utils

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.zeynekurtulus.wayfare.R

object OfflineUtils {
    
    /**
     * Show offline indicator snackbar
     */
    fun showOfflineSnackbar(view: View, context: Context) {
        Snackbar.make(
            view,
            "You're offline. Some features may be limited.",
            Snackbar.LENGTH_LONG
        ).apply {
            setBackgroundTint(context.getColor(R.color.warning_yellow_500))
            setTextColor(context.getColor(R.color.white))
        }.show()
    }
    
    /**
     * Show download success message
     */
    fun showDownloadSuccessSnackbar(view: View, routeName: String, context: Context) {
        Snackbar.make(
            view,
            "✓ '$routeName' downloaded for offline access",
            Snackbar.LENGTH_SHORT
        ).apply {
            setBackgroundTint(context.getColor(R.color.secondary_green))
            setTextColor(context.getColor(R.color.white))
        }.show()
    }
    
    /**
     * Show download failed message
     */
    fun showDownloadFailedSnackbar(view: View, error: String, context: Context) {
        Snackbar.make(
            view,
            "Download failed: $error",
            Snackbar.LENGTH_LONG
        ).apply {
            setBackgroundTint(context.getColor(R.color.error_red))
            setTextColor(context.getColor(R.color.white))
            setAction("Retry") {
                // Retry action can be implemented by the caller
            }
        }.show()
    }
    
    /**
     * Format storage size from bytes
     */
    fun formatStorageSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
    
    /**
     * Check if device has sufficient storage for downloads
     */
    fun hasEnoughStorage(context: Context, requiredBytes: Long = 100 * 1024 * 1024): Boolean {
        val availableBytes = context.filesDir.freeSpace
        return availableBytes > requiredBytes
    }
}