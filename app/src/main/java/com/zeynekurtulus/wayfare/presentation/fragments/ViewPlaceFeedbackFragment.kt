package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zeynekurtulus.wayfare.databinding.FragmentViewFeedbackBinding
import com.zeynekurtulus.wayfare.domain.model.Place
import com.zeynekurtulus.wayfare.utils.showToast

class ViewPlaceFeedbackFragment : Fragment() {
    
    private var _binding: FragmentViewFeedbackBinding? = null
    private val binding get() = _binding!!
    
    private var place: Place? = null
    
    companion object {
        private const val ARG_PLACE = "place"
        
        fun newInstance(place: Place): ViewPlaceFeedbackFragment {
            return ViewPlaceFeedbackFragment().apply {
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
        _binding = FragmentViewFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadPlaceData()
        setupClickListeners()
        showEmptyState()
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
        
        binding.retryButton.setOnClickListener {
            showToast("Place reviews feature coming soon!")
        }
    }
    
    private fun showEmptyState() {
        binding.apply {
            emptyStateLayout.visibility = View.VISIBLE
            feedbackRecyclerView.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            errorLayout.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}