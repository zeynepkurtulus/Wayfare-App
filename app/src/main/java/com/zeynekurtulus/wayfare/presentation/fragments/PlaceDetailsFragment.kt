package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentPlaceDetailsBinding
import com.zeynekurtulus.wayfare.domain.model.Place

class PlaceDetailsFragment : Fragment() {
    
    private var _binding: FragmentPlaceDetailsBinding? = null
    private val binding get() = _binding!!
    
    private var place: Place? = null
    
    companion object {
        private const val ARG_PLACE = "place"
        
        fun newInstance(place: Place): PlaceDetailsFragment {
            return PlaceDetailsFragment().apply {
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
        _binding = FragmentPlaceDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadPlaceData()
        setupClickListeners()
        displayPlaceDetails()
    }
    
    private fun loadPlaceData() {
        arguments?.let { args ->
            place = args.getParcelable(ARG_PLACE)
        }
    }
    
    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        binding.giveFeedbackButton.setOnClickListener {
            navigateToGivePlaceFeedback()
        }
        
        binding.viewFeedbackButton.setOnClickListener {
            navigateToViewPlaceFeedback()
        }
    }
    
    private fun displayPlaceDetails() {
        val currentPlace = place ?: return
        
        // Set place name
        binding.placeNameText.text = currentPlace.name
        
        // Set address
        if (currentPlace.address.isNullOrBlank()) {
            binding.addressText.visibility = View.GONE
        } else {
            binding.addressText.visibility = View.VISIBLE
            binding.addressText.text = currentPlace.address
        }
        
        // Set place image
        if (currentPlace.image.isNullOrBlank()) {
            binding.placeImageView.setImageResource(R.drawable.ic_placeholder)
        } else {
            Glide.with(this)
                .load(currentPlace.image)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .centerCrop()
                )
                .into(binding.placeImageView)
        }
        
        // Set rating
        if (currentPlace.rating != null && currentPlace.rating > 0) {
            binding.ratingCard.visibility = View.VISIBLE
            binding.ratingText.text = String.format("%.1f", currentPlace.rating)
        } else {
            binding.ratingCard.visibility = View.GONE
        }
        
        // Set duration
        if (currentPlace.duration != null && currentPlace.duration > 0) {
            binding.durationCard.visibility = View.VISIBLE
            val hours = currentPlace.duration / 60
            val minutes = currentPlace.duration % 60
            binding.durationText.text = if (hours > 0) {
                if (minutes > 0) {
                    "${hours}h ${minutes}m"
                } else {
                    "${hours}h"
                }
            } else {
                "${minutes}m"
            }
        } else {
            binding.durationCard.visibility = View.GONE
        }
        
        // Set category
        if (currentPlace.category.isNullOrBlank()) {
            binding.categoryLayout.visibility = View.GONE
        } else {
            binding.categoryLayout.visibility = View.VISIBLE
            binding.categoryChip.text = currentPlace.category
        }
        
        // Set opening hours
        if (currentPlace.openingHours.isNullOrEmpty()) {
            binding.openingHoursTitle.visibility = View.GONE
            binding.openingHoursLayout.visibility = View.GONE
        } else {
            binding.openingHoursTitle.visibility = View.VISIBLE
            binding.openingHoursLayout.visibility = View.VISIBLE
            setupOpeningHours(currentPlace.openingHours)
        }
    }
    
    private fun setupOpeningHours(openingHours: Map<String, String>) {
        binding.openingHoursLayout.removeAllViews()
        
        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        dayOrder.forEach { day ->
            val dayKey = day.lowercase()
            val hours = openingHours[dayKey]
            
            if (!hours.isNullOrBlank()) {
                val dayView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_opening_hours, binding.openingHoursLayout, false)
                
                val dayText = dayView.findViewById<android.widget.TextView>(R.id.dayText)
                val hoursText = dayView.findViewById<android.widget.TextView>(R.id.hoursText)
                
                dayText.text = day
                hoursText.text = hours
                
                binding.openingHoursLayout.addView(dayView)
            }
        }
    }
    
    private fun navigateToGivePlaceFeedback() {
        val currentPlace = place ?: return
        val fragment = GivePlaceFeedbackFragment.newInstance(currentPlace)
        
        // Access the activity's fragment manager for proper navigation
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("GivePlaceFeedback")
            .commit()
    }
    
    private fun navigateToViewPlaceFeedback() {
        val currentPlace = place ?: return
        val fragment = ViewPlaceFeedbackFragment.newInstance(currentPlace)
        
        // Access the activity's fragment manager for proper navigation
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("ViewPlaceFeedback")
            .commit()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}