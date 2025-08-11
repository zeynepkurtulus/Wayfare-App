package com.zeynekurtulus.wayfare.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import com.zeynekurtulus.wayfare.databinding.DialogBeautifulErrorBinding
import com.zeynekurtulus.wayfare.databinding.DialogBeautifulSuccessBinding
import com.zeynekurtulus.wayfare.databinding.DialogBeautifulDeleteBinding

object BeautifulDialogUtils {
    
    /**
     * Shows a beautiful error dialog with custom title and message
     */
    fun showErrorDialog(
        context: Context,
        title: String = "Error",
        message: String,
        buttonText: String = "Try Again",
        onButtonClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        // Inflate custom layout
        val binding = DialogBeautifulErrorBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        
        // Set dialog properties
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        // Set content
        binding.errorTitle.text = title
        binding.errorMessage.text = message
        binding.actionButton.text = buttonText
        
        // Set click listener
        binding.actionButton.setOnClickListener {
            dialog.dismiss()
            onButtonClick?.invoke()
        }
        
        // Make dialog cancelable
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        try {
            dialog.show()
        } catch (e: Exception) {
            // Handle case where activity might be finishing
            e.printStackTrace()
        }
    }
    
    /**
     * Shows a registration-specific error dialog
     */
    fun showRegistrationErrorDialog(
        context: Context,
        errorMessage: String,
        onRetry: (() -> Unit)? = null
    ) {
        val title = "Registration Failed"
        val message = when {
            errorMessage.contains("email", ignoreCase = true) -> 
                "This email address is already registered or invalid. Please use a different email address."
            errorMessage.contains("username", ignoreCase = true) -> 
                "This username is already taken. Please choose a different username."
            errorMessage.contains("password", ignoreCase = true) -> 
                "Password doesn't meet the requirements. Please ensure it's at least 8 characters long with numbers and letters."
            errorMessage.contains("network", ignoreCase = true) -> 
                "Network connection error. Please check your internet connection and try again."
            errorMessage.contains("server", ignoreCase = true) -> 
                "Server error occurred. Please try again in a few moments."
            else -> errorMessage.ifEmpty { "Registration failed. Please check your information and try again." }
        }
        
        showErrorDialog(
            context = context,
            title = title,
            message = message,
            buttonText = "Try Again",
            onButtonClick = onRetry
        )
    }
    
    /**
     * Shows a beautiful success dialog with custom title and message
     */
    fun showSuccessDialog(
        context: Context,
        title: String = "Success",
        message: String,
        primaryButtonText: String = "View Offline Downloads",
        secondaryButtonText: String = "Continue",
        onPrimaryButtonClick: (() -> Unit)? = null,
        onSecondaryButtonClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        // Inflate custom layout
        val binding = DialogBeautifulSuccessBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        
        // Set dialog properties
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        // Set content
        binding.successTitle.text = title
        binding.successMessage.text = message
        binding.actionButton.text = primaryButtonText
        binding.dismissButton.text = secondaryButtonText
        
        // Set click listeners
        binding.actionButton.setOnClickListener {
            dialog.dismiss()
            onPrimaryButtonClick?.invoke()
        }
        
        binding.dismissButton.setOnClickListener {
            dialog.dismiss()
            onSecondaryButtonClick?.invoke()
        }
        
        // Make dialog cancelable
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        try {
            dialog.show()
        } catch (e: Exception) {
            // Handle case where activity might be finishing
            e.printStackTrace()
        }
    }
    
    /**
     * Shows a beautiful download success dialog specifically for downloaded trips
     */
    fun showDownloadSuccessDialog(
        context: Context,
        tripName: String,
        onViewOfflineDownloads: (() -> Unit)? = null
    ) {
        showSuccessDialog(
            context = context,
            title = "Download Complete!",
            message = "'$tripName' has been downloaded successfully and is now available for offline access.",
            primaryButtonText = "View Offline Downloads",
            secondaryButtonText = "Continue",
            onPrimaryButtonClick = onViewOfflineDownloads
        )
    }
    
    /**
     * Shows a download failure dialog
     */
    fun showDownloadFailureDialog(
        context: Context,
        tripName: String,
        errorMessage: String,
        onRetry: (() -> Unit)? = null
    ) {
        val message = "Failed to download '$tripName'. $errorMessage"
        showErrorDialog(
            context = context,
            title = "Download Failed",
            message = message,
            buttonText = "Try Again",
            onButtonClick = onRetry
        )
    }
    
    /**
     * Shows a beautiful delete confirmation dialog
     */
    fun showDeleteConfirmationDialog(
        context: Context,
        title: String = "Remove Download",
        message: String,
        onDelete: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        // Inflate custom layout
        val binding = DialogBeautifulDeleteBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        
        // Set dialog properties
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        // Set content
        binding.deleteTitle.text = title
        binding.deleteMessage.text = message
        
        // Set click listeners
        binding.deleteButton.setOnClickListener {
            dialog.dismiss()
            onDelete?.invoke()
        }
        
        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
            onCancel?.invoke()
        }
        
        // Make dialog cancelable
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        try {
            dialog.show()
        } catch (e: Exception) {
            // Handle case where activity might be finishing
            e.printStackTrace()
        }
    }
    
    /**
     * Shows a delete confirmation dialog specifically for downloaded routes
     */
    fun showDeleteDownloadDialog(
        context: Context,
        routeName: String,
        onDelete: (() -> Unit)? = null
    ) {
        val message = "Remove '$routeName' from offline downloads?\n\nYou can download it again when you have internet connection."
        showDeleteConfirmationDialog(
            context = context,
            title = "Remove Download",
            message = message,
            onDelete = onDelete
        )
    }
}