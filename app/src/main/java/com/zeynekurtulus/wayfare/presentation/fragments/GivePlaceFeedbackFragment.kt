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
    
    override fun onPause() {
        super.onPause()
        
        // Check if user has unsaved changes and show warning if needed
        if (hasUnsavedChanges() && !isNavigatingBack) {
            showUnsavedChangesWarning()
        }
    }
    
    private var isNavigatingBack = false
    
    fun hasUnsavedChanges(): Boolean {
        // Since this is a placeholder, we can check if user started interacting with the form
        // For now, return false as this is not fully implemented
        return false
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
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        // Make dialog background white and dim the background
        dialog.window?.setBackgroundDrawableResource(com.zeynekurtulus.wayfare.R.drawable.bg_dialog_white)
        dialog.window?.setDimAmount(0.6f) // Dim the background
        
        dialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}