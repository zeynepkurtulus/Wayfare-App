package com.zeynekurtulus.wayfare.presentation.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.zeynekurtulus.wayfare.databinding.ActivityOtpVerificationBinding
import com.zeynekurtulus.wayfare.presentation.viewmodels.OtpVerificationViewModel
import com.zeynekurtulus.wayfare.utils.DialogUtils
import com.zeynekurtulus.wayfare.utils.NetworkUtils
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.domain.model.UserRegistration

/**
 * OtpVerificationActivity - Handles email verification via OTP
 * 
 * Features:
 * - 4-digit OTP input with automatic focus management
 * - Paste functionality for OTP codes
 * - Resend OTP with countdown timer
 * - Real-time validation and error handling
 * - Loading states and user feedback
 */
class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding
    
    private val viewModel: OtpVerificationViewModel by viewModels {
        getAppContainer().viewModelFactory
    }

    private lateinit var otpInputs: Array<EditText>
    private var userEmail: String = ""

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_REGISTRATION_DATA = "extra_registration_data"
        
        fun createIntent(context: Context, email: String, userRegistration: UserRegistration? = null): Intent {
            return Intent(context, OtpVerificationActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
                if (userRegistration != null) {
                    putExtra(EXTRA_REGISTRATION_DATA, userRegistration)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Remove action bar
        supportActionBar?.hide()

        // Get email from intent
        userEmail = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        
        // Get registration data from intent (if any)
        val registrationData = intent.getParcelableExtra<UserRegistration>(EXTRA_REGISTRATION_DATA)
        
        setupUI()
        setupOtpInputs()
        setupObservers()
        setupClickListeners()
        
        // Set email and registration data in ViewModel
        viewModel.setUserEmail(userEmail)
        viewModel.setPendingRegistration(registrationData)
    }

    private fun setupUI() {
        // Display email
        binding.emailText.text = userEmail
        
        // Initialize OTP inputs array (6 digits)
        otpInputs = arrayOf(
            binding.otpDigit1,
            binding.otpDigit2,
            binding.otpDigit3,
            binding.otpDigit4,
            binding.otpDigit5,
            binding.otpDigit6
        )
    }

    private fun setupOtpInputs() {
        otpInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    
                    // Update ViewModel
                    viewModel.updateOtpDigit(index, text)
                    
                    // Auto-focus management
                    if (text.isNotEmpty() && index < otpInputs.size - 1) {
                        // Move to next input
                        otpInputs[index + 1].requestFocus()
                    }
                    
                    // Handle paste - if user pastes 4-digit code
                    if (text.length > 1 && text.all { it.isDigit() }) {
                        handlePastedOtp(text, index)
                    }
                }
            })
            
            // Handle backspace
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        // Move to previous input and clear it
                        otpInputs[index - 1].requestFocus()
                        otpInputs[index - 1].setText("")
                        return@setOnKeyListener true
                    }
                }
                false
            }
            
            // Handle focus changes
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editText.selectAll()
                }
            }
        }
        
        // Set initial focus
        otpInputs[0].requestFocus()
    }

    private fun handlePastedOtp(pastedText: String, startIndex: Int) {
        val digits = pastedText.take(4).toCharArray()
        
        digits.forEachIndexed { i, digit ->
            val targetIndex = startIndex + i
            if (targetIndex < otpInputs.size) {
                otpInputs[targetIndex].setText(digit.toString())
                viewModel.updateOtpDigit(targetIndex, digit.toString())
            }
        }
        
        // Focus the last filled input or next empty one
        val lastIndex = minOf(startIndex + digits.size - 1, otpInputs.size - 1)
        if (lastIndex + 1 < otpInputs.size) {
            otpInputs[lastIndex + 1].requestFocus()
        } else {
            otpInputs[lastIndex].requestFocus()
        }
    }

    private fun setupObservers() {
        // Observe verification state
        viewModel.verificationState.observe(this, Observer { result ->
            when (result) {
                is ApiResult.Success -> {
                    DialogUtils.showAutoSuccessDialog(
                        context = this,
                        title = "Welcome to Wayfare! 🎉",
                        message = result.data
                    ) {
                        // Navigate to main activity after 3 seconds
                        navigateToMainActivity()
                    }
                }
                is ApiResult.Error -> {
                    DialogUtils.showErrorDialog(
                        context = this,
                        title = "Verification Failed",
                        message = result.message
                    ) {
                        // Clear OTP inputs for retry
                        clearOtpInputs()
                    }
                }
                is ApiResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        })

        // Observe loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.verifyButton.text = if (isLoading) "" else "Verify"
            binding.verifyButton.isEnabled = !isLoading
        })

        // Observe OTP error
        viewModel.otpError.observe(this, Observer { error ->
            if (error != null) {
                binding.otpErrorText.text = error
                binding.otpErrorText.visibility = View.VISIBLE
                // Highlight inputs with error state
                otpInputs.forEach { it.isSelected = true }
            } else {
                binding.otpErrorText.visibility = View.GONE
                // Remove error state
                otpInputs.forEach { it.isSelected = false }
            }
        })

        // Observe resend functionality
        viewModel.canResend.observe(this, Observer { canResend ->
            binding.resendCodeText.isEnabled = canResend
            binding.resendCodeText.alpha = if (canResend) 1.0f else 0.5f
            binding.resendCountdownText.visibility = if (canResend) View.GONE else View.VISIBLE
        })

        // Observe resend countdown
        viewModel.resendCountdown.observe(this, Observer { countdown ->
            binding.resendCountdownText.text = "Resend in ${countdown}s"
        })
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // Verify button
        binding.verifyButton.setOnClickListener {
            viewModel.verifyOtp()
        }

        // Resend code
        binding.resendCodeText.setOnClickListener {
            if (viewModel.canResend.value == true) {
                viewModel.resendOtp()
                clearOtpInputs()
                otpInputs[0].requestFocus()
            }
        }
    }

    private fun clearOtpInputs() {
        otpInputs.forEach { 
            it.setText("")
            it.isSelected = false
        }
        viewModel.clearOtp()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Allow user to go back to registration screen
        super.onBackPressed()
    }
    
    override fun onResume() {
        super.onResume()
        // Auto-fill OTP if available in clipboard
        checkClipboardForOtp()
    }
    
    private fun checkClipboardForOtp() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).text?.toString()
                
                // Check if clipboard contains a 4-digit OTP
                if (clipText != null && clipText.length == 4 && clipText.all { it.isDigit() }) {
                    // Auto-fill OTP
                    clipText.forEachIndexed { index, digit ->
                        if (index < otpInputs.size) {
                            otpInputs[index].setText(digit.toString())
                            viewModel.updateOtpDigit(index, digit.toString())
                        }
                    }
                    otpInputs.last().requestFocus()
                }
            }
        } catch (e: Exception) {
            // Ignore clipboard errors
        }
    }
}