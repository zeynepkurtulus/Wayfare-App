package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zeynekurtulus.wayfare.databinding.FragmentGiveFeedbackBinding
import com.zeynekurtulus.wayfare.domain.model.Place
import com.zeynekurtulus.wayfare.utils.showToast

class GivePlaceFeedbackFragment : Fragment() {
    
    private var _binding: FragmentGiveFeedbackBinding? = null
    private val binding get() = _binding!!
    
    private var place: Place? = null
    
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
        
        binding.submitFeedbackButton.setOnClickListener {
            showToast("Place feedback feature coming soon!")
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}