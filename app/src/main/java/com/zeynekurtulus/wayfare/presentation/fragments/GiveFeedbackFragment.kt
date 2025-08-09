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
import com.zeynekurtulus.wayfare.domain.model.CreateRouteFeedback
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.presentation.viewmodels.FeedbackViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import java.text.SimpleDateFormat
import java.util.*

class GiveFeedbackFragment : Fragment() {
    
    private var _binding: FragmentGiveFeedbackBinding? = null
    private val binding get() = _binding!!
    
    private val feedbackViewModel: FeedbackViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private var route: Route? = null
    private var selectedRating: Int = 0
    private var selectedDate: String = ""
    
    companion object {
        private const val ARG_ROUTE = "route"
        
        fun newInstance(route: Route): GiveFeedbackFragment {
            return GiveFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROUTE, route)
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
        
        loadRouteData()
        setupClickListeners()
        setupObservers()
        setupStarRating()
        setupDatePicker()
    }
    
    private fun loadRouteData() {
        arguments?.let { args ->
            route = args.getParcelable(ARG_ROUTE)
            route?.let { r ->
                binding.routeTitleText.text = r.title
                binding.routeDetailsText.text = "${r.city}, ${r.country} • ${calculateDuration(r.startDate, r.endDate)} Days • ${r.budget.capitalize()} Budget"
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.submitFeedbackButton.setOnClickListener {
            submitFeedback()
        }
    }
    
    private fun setupObservers() {
        feedbackViewModel.submitState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is com.zeynekurtulus.wayfare.presentation.viewmodels.SubmitFeedbackState.Success -> {
                    binding.submitFeedbackButton.isEnabled = true
                    binding.submitFeedbackButton.text = "Submit Feedback"
                    showToast("Feedback submitted successfully!")
                    parentFragmentManager.popBackStack()
                }
                is com.zeynekurtulus.wayfare.presentation.viewmodels.SubmitFeedbackState.Error -> {
                    binding.submitFeedbackButton.isEnabled = true
                    binding.submitFeedbackButton.text = "Submit Feedback"
                    showToast("Error: ${state.message}")
                    Log.e("GiveFeedbackFragment", "Feedback submission error: ${state.message}")
                }
                is com.zeynekurtulus.wayfare.presentation.viewmodels.SubmitFeedbackState.Idle -> {
                    binding.submitFeedbackButton.isEnabled = selectedRating > 0 && selectedDate.isNotEmpty()
                    binding.submitFeedbackButton.text = "Submit Feedback"
                }
            }
        })
        
        feedbackViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.submitFeedbackButton.isEnabled = false
                binding.submitFeedbackButton.text = "Submitting..."
            } else {
                updateSubmitButton()
            }
        })
    }
    
    private fun setupStarRating() {
        val stars = listOf(
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        )
        
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStarDisplay()
                updateSubmitButton()
                updateRatingText()
            }
        }
    }
    
    private fun updateStarDisplay() {
        val stars = listOf(
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        )
        
        stars.forEachIndexed { index, star ->
            if (index < selectedRating) {
                star.setImageResource(R.drawable.ic_star_filled)
            } else {
                star.setImageResource(R.drawable.ic_star_outline)
            }
        }
    }
    
    private fun updateRatingText() {
        val ratingTexts = arrayOf(
            "Poor", "Fair", "Good", "Very Good", "Excellent"
        )
        
        if (selectedRating > 0) {
            binding.ratingText.text = "${selectedRating}/5 - ${ratingTexts[selectedRating - 1]}"
        } else {
            binding.ratingText.text = "Tap stars to rate"
        }
    }
    
    private fun setupDatePicker() {
        binding.visitDateEditText.setOnClickListener {
            showDatePicker()
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
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time)
                
                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.visitDateEditText.setText(displayFormat.format(calendar.time))
                
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
        val route = this.route ?: return
        
        if (selectedRating == 0) {
            showToast("Please select a rating")
            return
        }
        
        if (selectedDate.isEmpty()) {
            showToast("Please select a visit date")
            return
        }
        
        val comment = binding.commentEditText.text?.toString()?.trim()
        
        Log.d("GiveFeedbackFragment", "Submitting feedback: rating=$selectedRating, date=$selectedDate, comment=$comment")
        feedbackViewModel.submitRouteFeedback(
            routeId = route.routeId,
            rating = selectedRating,
            comment = comment,
            visitedOn = selectedDate
        )
    }
    
    private fun calculateDuration(startDate: String, endDate: String): Int {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = format.parse(startDate)
            val end = format.parse(endDate)
            val diffInMillis = end!!.time - start!!.time
            val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            diffInDays + 1 // Include both start and end days
        } catch (e: Exception) {
            Log.e("GiveFeedbackFragment", "Error calculating duration", e)
            1
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}