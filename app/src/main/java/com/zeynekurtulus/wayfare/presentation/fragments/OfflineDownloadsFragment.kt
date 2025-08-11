package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.zeynekurtulus.wayfare.databinding.FragmentOfflineDownloadsBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.presentation.adapters.MyTripsAdapter
import com.zeynekurtulus.wayfare.presentation.viewmodels.OfflineRouteViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.DownloadProgress
import com.zeynekurtulus.wayfare.presentation.viewmodels.SyncStatus
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.BeautifulDialogUtils
import kotlinx.coroutines.launch

class OfflineDownloadsFragment : Fragment() {
    
    private var _binding: FragmentOfflineDownloadsBinding? = null
    private val binding get() = _binding!!
    
    private val offlineRouteViewModel: OfflineRouteViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private lateinit var downloadedRoutesAdapter: MyTripsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfflineDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
        
        // Load downloaded routes
        offlineRouteViewModel.loadDownloadedRoutes()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupRecyclerView() {
        downloadedRoutesAdapter = MyTripsAdapter(
            isGridLayout = false,
            onTripClick = { route ->
                // Navigate to route details
                navigateToRouteDetails(route)
            },
            onMenuClick = { route, view ->
                showRouteMenu(route, view)
            },
            onDownloadClick = null, // No download action needed here since these are already downloaded
            isRouteDownloaded = { routeId ->
                offlineRouteViewModel.isRouteDownloaded(routeId)
            }
        )
        
        binding.downloadedRoutesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = downloadedRoutesAdapter
        }
    }
    
    private fun observeViewModel() {
        // Observe downloaded routes
        offlineRouteViewModel.downloadedRoutes.observe(viewLifecycleOwner) { routes ->
            updateRoutesList(routes)
            updateDownloadedCount(routes.size)
        }
        
        // Observe network connectivity
        offlineRouteViewModel.isNetworkAvailable.observe(viewLifecycleOwner) { isConnected ->
            updateNetworkStatus(isConnected)
        }
        
        // Observe sync status
        offlineRouteViewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            updateSyncStatus(status)
        }
        
        // Observe download progress
        lifecycleScope.launch {
            offlineRouteViewModel.downloadProgress.collect { progressMap ->
                // Handle download progress if needed
                progressMap.forEach { (routeId, progress) ->
                    when (progress) {
                        is DownloadProgress.Completed -> {
                            showSnackbar("Route downloaded successfully")
                        }
                        is DownloadProgress.Failed -> {
                            showSnackbar("Download failed: ${progress.error}")
                        }
                        is DownloadProgress.Downloading -> {
                            // Show downloading progress if needed
                        }
                    }
                }
            }
        }
        
        // Observe errors
        offlineRouteViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showSnackbar(it)
                offlineRouteViewModel.clearError()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.syncButton.setOnClickListener {
            if (offlineRouteViewModel.isNetworkAvailable.value == true) {
                offlineRouteViewModel.syncWithServer()
            } else {
                showSnackbar("No internet connection available")
            }
        }
        
        binding.clearCacheButton.setOnClickListener {
            showClearCacheDialog()
        }
    }
    
    private fun updateRoutesList(routes: List<Route>) {
        downloadedRoutesAdapter.updateTrips(routes)
        
        if (routes.isEmpty()) {
            binding.downloadedRoutesRecyclerView.visibility = View.GONE
            binding.emptyStateContainer.visibility = View.VISIBLE
        } else {
            binding.downloadedRoutesRecyclerView.visibility = View.VISIBLE
            binding.emptyStateContainer.visibility = View.GONE
        }
    }
    
    private fun updateDownloadedCount(count: Int) {
        binding.downloadedCountText.text = count.toString()
    }
    
    private fun updateNetworkStatus(isConnected: Boolean) {
        if (isConnected) {
            binding.networkStatusBanner.visibility = View.GONE
            binding.syncButton.isEnabled = true
        } else {
            binding.networkStatusBanner.visibility = View.VISIBLE
            binding.networkStatusText.text = "No internet connection. Showing offline content only."
            binding.syncButton.isEnabled = false
        }
    }
    
    private fun updateSyncStatus(status: SyncStatus) {
        when (status) {
            is SyncStatus.Syncing -> {
                binding.syncButton.text = "Syncing..."
                binding.syncButton.isEnabled = false
            }
            is SyncStatus.Completed -> {
                binding.syncButton.text = "Sync Now"
                binding.syncButton.isEnabled = true
                showSnackbar("Sync completed successfully")
            }
            is SyncStatus.Failed -> {
                binding.syncButton.text = "Sync Now"
                binding.syncButton.isEnabled = true
                showSnackbar("Sync failed: ${status.error}")
            }
            is SyncStatus.Idle -> {
                binding.syncButton.text = "Sync Now"
                binding.syncButton.isEnabled = offlineRouteViewModel.isNetworkAvailable.value ?: false
            }
        }
    }
    
    private fun navigateToRouteDetails(route: Route) {
        // Navigate to route details fragment
        // You can implement this based on your navigation setup
    }
    
    private fun showRouteMenu(route: Route, anchorView: View) {
        BeautifulDialogUtils.showDeleteDownloadDialog(
            context = requireContext(),
            routeName = route.title,
            onDelete = {
                offlineRouteViewModel.removeDownloadedRoute(route.routeId)
            }
        )
    }
    
    private fun showClearCacheDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear Cache")
            .setMessage("This will remove old cached data to free up storage space. Downloaded routes will not be affected.")
            .setPositiveButton("Clear") { _, _ ->
                offlineRouteViewModel.clearOldCache()
                showSnackbar("Cache cleared successfully")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}