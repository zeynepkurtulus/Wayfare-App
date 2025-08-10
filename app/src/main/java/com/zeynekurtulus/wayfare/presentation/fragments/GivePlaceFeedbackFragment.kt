package com.zeynekurtulus.wayfare.presentation.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentGiveFeedbackBinding
import com.zeynekurtulus.wayfare.domain.model.Place
import com.zeynekurtulus.wayfare.presentation.viewmodels.SubmitFeedbackState
import com.zeynekurtulus.wayfare.presentation.viewmodels.FeedbackViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import java.text.SimpleDateFormat
import java.util.*

class GivePlaceFeedbackFragment : Fragment() {
    
    private var _binding: FragmentGiveFeedbackBinding? = null
    private val binding get() = _binding!!
    
    private val feedbackViewModel: FeedbackViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private var place: Place? = null
    private var selectedRating: Int = 0
    private var selectedDate: String = ""
    
    companion object {
        private const val ARG_PLACE = "place"
        
        fun newInstance(place: Place): GivePlaceFeedbackFragment {
            return GivePlaceFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PLACE, place)
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiveFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadPlaceData()
        setupClickListeners()
        setupObservers()
        setupRatingStars()
    }
    
    private fun loadPlaceData() {
        arguments?.let { args ->
            place = args.getParcelable(ARG_PLACE)
            place?.let { p ->
                binding.routeTitleText.text = p.name
                binding.routeDetailsText.text = "${p.address ?: "Location unknown"}"
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        binding.visitDateEditText.setOnClickListener {
            showDatePicker()
        }
        
        binding.submitFeedbackButton.setOnClickListener {
            submitFeedback()
        }
    }
    
    private fun setupObservers() {
        feedbackViewModel.submitState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is SubmitFeedbackState.Loading -> {
                    binding.submitFeedbackButton.isEnabled = false
                    binding.submitFeedbackButton.text = "Submitting..."
                }
                is SubmitFeedbackState.Success -> {
                    binding.submitFeedbackButton.isEnabled = true
                    binding.submitFeedbackButton.text = "Submit Feedback"
                    showToast("Feedback submitted successfully!")
                    requireActivity().supportFragmentManager.popBackStack()
                }
                is SubmitFeedbackState.Error -> {
                    binding.submitFeedbackButton.isEnabled = true
                    binding.submitFeedbackButton.text = "Submit Feedback"
                    showToast("Failed to submit feedback: ${state.message}")
                }
                is SubmitFeedbackState.Idle -> {
                    binding.submitFeedbackButton.isEnabled = true
                    binding.submitFeedbackButton.text = "Submit Feedback"
                }
            }
        })
    }
    
    private fun setupRatingStars() {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )
        
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateRatingDisplay()
                updateSubmitButton()
                Log.d("GivePlaceFeedbackFragment", "Rating selected: $selectedRating")
            }
        }
    }
    
    private fun updateRatingDisplay() {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )
        
        stars.forEachIndexed { index, star ->
            if (index < selectedRating) {
                star.setImageResource(R.drawable.ic_star_filled)
            } else {
                star.setImageResource(R.drawable.ic_star_outline)
            }
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(selectedCalendar.time)
                
                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.visitDateEditText.setText(displayFormat.format(selectedCalendar.time))
                
                updateSubmitButton()
            },
            year, month, day
        )
        
        // Set max date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun updateSubmitButton() {
        binding.submitFeedbackButton.isEnabled = selectedRating > 0 && selectedDate.isNotEmpty()
    }
    
    private fun submitFeedback() {
        val currentPlace = this.place ?: return
        
        if (selectedRating == 0) {
            showToast("Please select a rating")
            return
        }
        
        if (selectedDate.isEmpty()) {
            showToast("Please select a visit date")
            return
        }
        
        val comment = binding.commentEditText.text?.toString()?.trim()
        
        Log.d("GivePlaceFeedbackFragment", "Submitting place feedback: rating=$selectedRating, date=$selectedDate, comment=$comment")
        feedbackViewModel.submitPlaceFeedback(
            placeId = currentPlace.placeId,
            rating = selectedRating,
            comment = comment,
            visitedOn = selectedDate
        )
    }
    
    override fun onPause() {
        super.onPause()
        
        // Check if user has unsaved changes and show warning if needed
        if (hasUnsavedChanges() && !isNavigatingBack) {
            showUnsavedChangesWarning()
        }
    }
    
    private var isNavigatingBack = false
    
    fun hasUnsavedChanges(): Boolean {
        val comment = binding.commentEditText.text?.toString()?.trim() ?: ""
        return selectedRating > 0 || comment.isNotEmpty() || selectedDate.isNotEmpty()
    }
    
    private fun showUnsavedChangesWarning() {
        if (!isAdded || activity == null) return
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Unsaved Changes")
        builder.setMessage("You have unsaved feedback. If you leave now, your feedback will be lost.\n\nAre you sure you want to continue?")
        
        // Create custom view for better styling
        val dialogView = layoutInflater.inflate(com.zeynekurtulus.wayfare.R.layout.dialog_unsaved_changes, null)
        builder.setView(dialogView)
        
        val dialog = builder.create()
        
        // Find buttons in custom layout
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(com.zeynekurtulus.wayfare.R.id.cancelButton)
        val continueButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(com.zeynekurtulus.wayfare.R.id.continueButton)
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        continueButton.setOnClickListener {
            dialog.dismiss()
            isNavigatingBack = true
            resetFeedbackForm()
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        // Make dialog background white and dim the background
        dialog.window?.setBackgroundDrawableResource(com.zeynekurtulus.wayfare.R.drawable.bg_dialog_white)
        dialog.window?.setDimAmount(0.6f) // Dim the background
        
        dialog.show()
    }
    
    private fun resetFeedbackForm() {
        selectedRating = 0
        selectedDate = ""
        binding.commentEditText.setText("")
        binding.visitDateEditText.setText("")
        updateRatingDisplay()
        updateSubmitButton()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}