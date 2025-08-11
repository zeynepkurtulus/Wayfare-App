package com.zeynekurtulus.wayfare.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.domain.repository.RouteRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkConnectivityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfflineRouteViewModel(
    private val routeRepository: RouteRepository,
    private val networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {
    
    private val _downloadedRoutes = MutableLiveData<List<Route>>()
    val downloadedRoutes: LiveData<List<Route>> = _downloadedRoutes
    
    private val _downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable: LiveData<Boolean> = _isNetworkAvailable
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus
    
    init {
        // Monitor network connectivity
        viewModelScope.launch {
            networkConnectivityManager.networkConnectivityFlow().collect { isConnected ->
                _isNetworkAvailable.value = isConnected
            }
        }
        
        // Clean up orphaned downloads and load downloaded routes
        viewModelScope.launch {
            cleanupOrphanedDownloads()
            loadDownloadedRoutes()
        }
        
        // Check initial network status
        _isNetworkAvailable.value = networkConnectivityManager.isNetworkAvailable()
    }
    
    fun downloadRoute(routeId: String) {
        viewModelScope.launch {
            try {
                // Update progress to downloading
                updateDownloadProgress(routeId, DownloadProgress.Downloading)
                
                when (val result = routeRepository.downloadRoute(routeId)) {
                    is ApiResult.Success -> {
                        updateDownloadProgress(routeId, DownloadProgress.Completed)
                        loadDownloadedRoutes() // Refresh the list
                        android.util.Log.d("OfflineRouteViewModel", "✅ Route downloaded successfully: $routeId")
                    }
                    is ApiResult.Error -> {
                        updateDownloadProgress(routeId, DownloadProgress.Failed(result.message))
                        _error.value = "Failed to download route: ${result.message}"
                        android.util.Log.e("OfflineRouteViewModel", "❌ Download failed: ${result.message}")
                    }
                    else -> {
                        updateDownloadProgress(routeId, DownloadProgress.Failed("Unknown error"))
                        _error.value = "Failed to download route"
                    }
                }
            } catch (e: Exception) {
                updateDownloadProgress(routeId, DownloadProgress.Failed(e.message ?: "Unknown error"))
                _error.value = "Download error: ${e.message}"
                android.util.Log.e("OfflineRouteViewModel", "❌ Exception during download: ${e.message}", e)
            }
        }
    }
    
    fun removeDownloadedRoute(routeId: String) {
        viewModelScope.launch {
            try {
                when (val result = routeRepository.removeDownloadedRoute(routeId)) {
                    is ApiResult.Success -> {
                        // Immediately update the local list to reflect the removal
                        val currentDownloadedRoutes = _downloadedRoutes.value?.toMutableList() ?: mutableListOf()
                        currentDownloadedRoutes.removeAll { it.routeId == routeId }
                        _downloadedRoutes.value = currentDownloadedRoutes
                        
                        // Also refresh from database to ensure consistency
                        loadDownloadedRoutes()
                        removeDownloadProgress(routeId)
                        android.util.Log.d("OfflineRouteViewModel", "✅ Route download removed: $routeId")
                    }
                    is ApiResult.Error -> {
                        _error.value = "Failed to remove downloaded route: ${result.message}"
                        android.util.Log.e("OfflineRouteViewModel", "❌ Remove failed: ${result.message}")
                    }
                    else -> {
                        _error.value = "Failed to remove downloaded route"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Remove error: ${e.message}"
                android.util.Log.e("OfflineRouteViewModel", "❌ Exception during remove: ${e.message}", e)
            }
        }
    }
    
    fun isRouteDownloaded(routeId: String): Boolean {
        val isDownloaded = _downloadedRoutes.value?.any { it.routeId == routeId } ?: false
        android.util.Log.d("OfflineRouteViewModel", "🔍 isRouteDownloaded($routeId): $isDownloaded (total downloaded: ${_downloadedRoutes.value?.size ?: 0})")
        return isDownloaded
    }
    
    fun loadDownloadedRoutes() {
        viewModelScope.launch {
            try {
                when (val result = routeRepository.getDownloadedRoutesForCurrentUser()) {
                    is ApiResult.Success -> {
                        _downloadedRoutes.value = result.data
                        android.util.Log.d("OfflineRouteViewModel", "✅ Loaded ${result.data.size} downloaded routes for current user")
                    }
                    is ApiResult.Error -> {
                        _error.value = "Failed to load downloaded routes: ${result.message}"
                        android.util.Log.e("OfflineRouteViewModel", "❌ Load failed: ${result.message}")
                    }
                    else -> {
                        _error.value = "Failed to load downloaded routes"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Load error: ${e.message}"
                android.util.Log.e("OfflineRouteViewModel", "❌ Exception during load: ${e.message}", e)
            }
        }
    }
    
    fun syncWithServer() {
        viewModelScope.launch {
            try {
                _syncStatus.value = SyncStatus.Syncing
                
                when (val result = routeRepository.syncWithServer()) {
                    is ApiResult.Success -> {
                        _syncStatus.value = SyncStatus.Completed
                        loadDownloadedRoutes() // Refresh to show updated data
                        android.util.Log.d("OfflineRouteViewModel", "✅ Sync completed successfully")
                    }
                    is ApiResult.Error -> {
                        _syncStatus.value = SyncStatus.Failed(result.message)
                        _error.value = "Sync failed: ${result.message}"
                        android.util.Log.e("OfflineRouteViewModel", "❌ Sync failed: ${result.message}")
                    }
                    else -> {
                        _syncStatus.value = SyncStatus.Failed("Unknown error")
                        _error.value = "Sync failed"
                    }
                }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Failed(e.message ?: "Unknown error")
                _error.value = "Sync error: ${e.message}"
                android.util.Log.e("OfflineRouteViewModel", "❌ Exception during sync: ${e.message}", e)
            }
        }
    }
    
    fun clearOldCache(days: Int = 7) {
        viewModelScope.launch {
            try {
                when (val result = routeRepository.clearOldCache(days)) {
                    is ApiResult.Success -> {
                        android.util.Log.d("OfflineRouteViewModel", "✅ Cache cleared successfully")
                    }
                    is ApiResult.Error -> {
                        _error.value = "Failed to clear cache: ${result.message}"
                        android.util.Log.e("OfflineRouteViewModel", "❌ Cache clear failed: ${result.message}")
                    }
                    else -> {
                        _error.value = "Failed to clear cache"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Cache clear error: ${e.message}"
                android.util.Log.e("OfflineRouteViewModel", "❌ Exception during cache clear: ${e.message}", e)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private suspend fun cleanupOrphanedDownloads() {
        try {
            when (val result = routeRepository.cleanupOrphanedDownloads()) {
                is ApiResult.Success -> {
                    android.util.Log.d("OfflineRouteViewModel", "✅ Cleanup completed successfully")
                }
                is ApiResult.Error -> {
                    android.util.Log.e("OfflineRouteViewModel", "❌ Cleanup failed: ${result.message}")
                }
                else -> {
                    android.util.Log.e("OfflineRouteViewModel", "❌ Cleanup failed")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("OfflineRouteViewModel", "❌ Exception during cleanup: ${e.message}", e)
        }
    }
    
    private fun updateDownloadProgress(routeId: String, progress: DownloadProgress) {
        val currentProgress = _downloadProgress.value.toMutableMap()
        currentProgress[routeId] = progress
        _downloadProgress.value = currentProgress
    }
    
    private fun removeDownloadProgress(routeId: String) {
        val currentProgress = _downloadProgress.value.toMutableMap()
        currentProgress.remove(routeId)
        _downloadProgress.value = currentProgress
    }
}

sealed class DownloadProgress {
    object Downloading : DownloadProgress()
    object Completed : DownloadProgress()
    data class Failed(val error: String) : DownloadProgress()
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    object Completed : SyncStatus()
    data class Failed(val error: String) : SyncStatus()
}