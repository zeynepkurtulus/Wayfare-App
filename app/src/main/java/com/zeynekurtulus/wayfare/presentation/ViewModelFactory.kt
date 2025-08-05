package com.zeynekurtulus.wayfare.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zeynekurtulus.wayfare.domain.repository.*
import com.zeynekurtulus.wayfare.presentation.viewmodels.LoginViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.RegisterViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.SignUpViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.TripMakerViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.OtpVerificationViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.FeedbackViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.LocationViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.PlaceViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteDetailViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.UserProfileViewModel

/**
 * Factory for creating ViewModels with proper dependencies
 */
class ViewModelFactory(
    private val userRepository: UserRepository,
    private val routeRepository: RouteRepository,
    private val placeRepository: PlaceRepository,
    private val locationRepository: LocationRepository,
    private val feedbackRepository: FeedbackRepository,
    private val cityRepository: CityRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(userRepository) as T
            RegisterViewModel::class.java -> RegisterViewModel(userRepository) as T
            SignUpViewModel::class.java -> SignUpViewModel(userRepository) as T
            TripMakerViewModel::class.java -> TripMakerViewModel(cityRepository, routeRepository) as T
            OtpVerificationViewModel::class.java -> OtpVerificationViewModel(userRepository) as T
            UserProfileViewModel::class.java -> UserProfileViewModel(userRepository) as T
            RouteListViewModel::class.java -> RouteListViewModel(routeRepository) as T
            RouteDetailViewModel::class.java -> RouteDetailViewModel(routeRepository) as T
            PlaceViewModel::class.java -> PlaceViewModel(placeRepository) as T
            LocationViewModel::class.java -> LocationViewModel(locationRepository) as T
            FeedbackViewModel::class.java -> FeedbackViewModel(feedbackRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}