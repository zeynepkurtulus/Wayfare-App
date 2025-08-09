package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeynekurtulus.wayfare.databinding.FragmentViewFeedbackBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.presentation.adapters.FeedbackAdapter
import com.zeynekurtulus.wayfare.presentation.viewmodels.FeedbackViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer

class ViewFeedbackFragment : Fragment() {
    
    private var _binding: FragmentViewFeedbackBinding? = null
    private val binding get() = _binding!!
    
    private val feedbackViewModel: FeedbackViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private lateinit var feedbackAdapter: FeedbackAdapter
    private var route: Route? = null
    
    companion object {
        private const val ARG_ROUTE = "route"
        
        fun newInstance(route: Route): ViewFeedbackFragment {
            return ViewFeedbackFragment().apply {
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
        _binding = FragmentViewFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadRouteData()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        loadFeedback()
    }
    
    private fun loadRouteData() {
        arguments?.let { args ->
            route = args.getParcelable(ARG_ROUTE)
        }
    }
    
    private fun setupRecyclerView() {
        feedbackAdapter = FeedbackAdapter()
        binding.feedbackRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedbackAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.retryButton.setOnClickListener {
            loadFeedback()
        }
    }
    
    private fun setupObservers() {
        feedbackViewModel.routeFeedback.observe(viewLifecycleOwner, Observer { feedbackList ->
            if (feedbackList.isNotEmpty()) {
                showFeedbackList(feedbackList)
                Log.d("ViewFeedbackFragment", "Loaded ${feedbackList.size} feedback entries")
            } else {
                showEmptyState()
                Log.d("ViewFeedbackFragment", "No feedback found for route")
            }
        })
        
        feedbackViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                showLoadingState()
                Log.d("ViewFeedbackFragment", "Loading feedback...")
            }
        })
        
        feedbackViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("ViewFeedbackFragment", "Error loading feedback: $it")
                
                // If the error indicates no feedback exists or API endpoint not found, show empty state
                if (it.contains("404") || it.contains("not found") || it.contains("no feedback") || 
                    it.contains("no reviews") || it.contains("Failed to get route feedback")) {
                    Log.d("ViewFeedbackFragment", "Treating as empty feedback instead of error")
                    showEmptyState()
                } else {
                    showErrorState(it)
                }
            }
        })
    }
    
    private fun loadFeedback() {
        route?.let { r ->
            Log.d("ViewFeedbackFragment", "Loading feedback for route: ${r.routeId}")
            Log.d("ViewFeedbackFragment", "Route details: title='${r.title}', city='${r.city}', isPublic=${r.isPublic}")
            Log.d("ViewFeedbackFragment", "Route ID length: ${r.routeId.length}")
            Log.d("ViewFeedbackFragment", "Route userId: ${r.userId}")
            feedbackViewModel.getRouteFeedback(r.routeId)
        } ?: run {
            Log.e("ViewFeedbackFragment", "Route object is null!")
        }
    }
    
    private fun showLoadingState() {
        binding.apply {
            loadingLayout.visibility = View.VISIBLE
            feedbackRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            errorLayout.visibility = View.GONE
        }
    }
    
    private fun showFeedbackList(feedback: List<com.zeynekurtulus.wayfare.domain.model.RouteFeedback>) {
        feedbackAdapter.updateFeedback(feedback)
        binding.apply {
            loadingLayout.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            errorLayout.visibility = View.GONE
            feedbackRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun showEmptyState() {
        binding.apply {
            loadingLayout.visibility = View.GONE
            feedbackRecyclerView.visibility = View.GONE
            errorLayout.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            loadingLayout.visibility = View.GONE
            feedbackRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            errorMessageText.text = message
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}