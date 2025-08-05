package com.zeynekurtulus.wayfare.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import com.zeynekurtulus.wayfare.databinding.DialogBeautifulErrorBinding

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
}