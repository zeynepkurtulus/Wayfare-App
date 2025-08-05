package com.zeynekurtulus.wayfare.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import com.zeynekurtulus.wayfare.R

object DialogUtils {
    
    /**
     * Shows a success dialog with green theme
     */
    fun showSuccessDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "Continue",
        onButtonClick: (() -> Unit)? = null
    ) {
        showCustomDialog(
            context = context,
            title = title,
            message = message,
            buttonText = buttonText,
            iconRes = R.drawable.ic_success,
            isSuccess = true,
            onButtonClick = onButtonClick
        )
    }
    
    /**
     * Shows an error dialog with red theme
     */
    fun showErrorDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "Try Again",
        onButtonClick: (() -> Unit)? = null
    ) {
        showCustomDialog(
            context = context,
            title = title,
            message = message,
            buttonText = buttonText,
            iconRes = R.drawable.ic_error,
            isSuccess = false,
            onButtonClick = onButtonClick
        )
    }
    
    /**
     * Shows an info dialog with blue theme
     */
    fun showInfoDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "OK",
        onButtonClick: (() -> Unit)? = null
    ) {
        showCustomDialog(
            context = context,
            title = title,
            message = message,
            buttonText = buttonText,
            iconRes = R.drawable.ic_info,
            isSuccess = null,
            onButtonClick = onButtonClick
        )
    }
    
    /**
     * Creates and shows a custom dialog
     */
    private fun showCustomDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String,
        iconRes: Int,
        isSuccess: Boolean?,
        onButtonClick: (() -> Unit)?
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_feedback, null)
        dialog.setContentView(view)
        
        // Find views
        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        val actionButton = view.findViewById<Button>(R.id.actionButton)
        
        // Set content
        iconImageView.setImageResource(iconRes)
        titleTextView.text = title
        messageTextView.text = message
        actionButton.text = buttonText
        
        // Set theme colors
        when (isSuccess) {
            true -> {
                // Success theme - green
                iconImageView.setColorFilter(context.getColor(R.color.success_green))
                titleTextView.setTextColor(context.getColor(R.color.success_green))
                actionButton.setBackgroundColor(context.getColor(R.color.success_green))
            }
            false -> {
                // Error theme - red
                iconImageView.setColorFilter(context.getColor(R.color.error_red))
                titleTextView.setTextColor(context.getColor(R.color.error_red))
                actionButton.setBackgroundColor(context.getColor(R.color.error_red))
            }
            null -> {
                // Info theme - blue (app primary)
                iconImageView.setColorFilter(context.getColor(R.color.primary))
                titleTextView.setTextColor(context.getColor(R.color.primary))
                actionButton.setBackgroundColor(context.getColor(R.color.primary))
            }
        }
        
        // Set button click listener
        actionButton.setOnClickListener {
            dialog.dismiss()
            onButtonClick?.invoke()
        }
        
        // Make dialog cancelable
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        // Set dialog size
        val width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 300f, context.resources.displayMetrics
        ).toInt()
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200f, context.resources.displayMetrics
        ).toInt()
        dialog.window?.setLayout(width, height)
        
        // Show dialog
        dialog.show()
    }
    
    /**
     * Shows a loading dialog
     */
    fun showLoadingDialog(context: Context, message: String = "Please wait..."): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        dialog.setContentView(view)
        
        val messageTextView = view.findViewById<TextView>(R.id.loadingMessageTextView)
        messageTextView.text = message
        
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Set dialog size
        val width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 300f, context.resources.displayMetrics
        ).toInt()
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200f, context.resources.displayMetrics
        ).toInt()
        dialog.window?.setLayout(width, height)
        
        dialog.show()
        return dialog
    }
    
    /**
     * Shows a success dialog that auto-closes after 3 seconds and executes callback
     */
    fun showAutoSuccessDialog(
        context: Context,
        title: String,
        message: String,
        onAutoClose: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_feedback, null)
        dialog.setContentView(view)
        
        // Find views
        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        val actionButton = view.findViewById<Button>(R.id.actionButton)
        
        // Set content
        iconImageView.setImageResource(R.drawable.ic_success)
        titleTextView.text = title
        messageTextView.text = message
        
        // Hide the button since this dialog auto-closes
        actionButton.visibility = View.GONE
        
        // Set success theme colors
        iconImageView.setColorFilter(context.getColor(R.color.success_green))
        titleTextView.setTextColor(context.getColor(R.color.success_green))
        
        // Make dialog non-cancelable during the 3 seconds
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Set dialog size
        val width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 300f, context.resources.displayMetrics
        ).toInt()
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200f, context.resources.displayMetrics
        ).toInt()
        dialog.window?.setLayout(width, height)
        
        dialog.show()
        
        // Auto-close after 3 seconds and execute callback
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onAutoClose?.invoke()
            }
        }, 3000)
    }
    
    /**
     * Shows an error dialog that auto-closes after 3 seconds and executes callback
     */
    fun showAutoErrorDialog(
        context: Context,
        title: String,
        message: String,
        onAutoClose: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_feedback, null)
        dialog.setContentView(view)
        
        // Find views
        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        val actionButton = view.findViewById<Button>(R.id.actionButton)
        
        // Set content
        iconImageView.setImageResource(R.drawable.ic_error)
        titleTextView.text = title
        messageTextView.text = message
        
        // Hide the button since this dialog auto-closes
        actionButton.visibility = View.GONE
        
        // Set error theme colors
        iconImageView.setColorFilter(context.getColor(R.color.error_red))
        titleTextView.setTextColor(context.getColor(R.color.error_red))
        
        // Make dialog non-cancelable during the 3 seconds
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Set dialog size
        val width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 300f, context.resources.displayMetrics
        ).toInt()
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200f, context.resources.displayMetrics
        ).toInt()
        dialog.window?.setLayout(width, height)
        
        dialog.show()
        
        // Auto-close after 3 seconds and execute callback
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onAutoClose?.invoke()
            }
        }, 3000)
    }
}